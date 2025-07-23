package org.example.operatormanagementsystem.customer_thai.service.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.example.operatormanagementsystem.customer_thai.dto.ObjectDimensionsDTO;
import org.example.operatormanagementsystem.customer_thai.dto.ObjectsAnalysisResultDTO;
import org.example.operatormanagementsystem.customer_thai.service.ImageAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class ImageAnalysisServiceImpl implements ImageAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(ImageAnalysisServiceImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OkHttpClient httpClient = new OkHttpClient();

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openAiApiUrl;

    @Override
    public ObjectDimensionsDTO analyzeObjectDimensions(MultipartFile imageFile) {
        // Phương thức này có thể được cải thiện tương tự như analyzeAllObjectsDimensions nếu cần
        return createSampleObjectData(imageFile.getOriginalFilename());
    }

    @Override
    public ObjectsAnalysisResultDTO analyzeAllObjectsDimensions(MultipartFile imageFile) {
        try {
            String base64Image = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageFile.getBytes());

            String prompt = "BẠN LÀ CHUYÊN GIA NHẬN DẠNG VẬT THỂ cho một trang web vận chuyển nhà. Nhiệm vụ của bạn là phân tích hình ảnh và xác định tên chính xác cho MỌI vật thể phạm vi vật thể là các vật dụng trong Nhà tiêu chuẩnchuẩn. " +
                            "Tên vật thể là thông tin QUAN TRỌNG NHẤT. BẮT BUỘC phải đặt tên cụ thể, không được dùng tên chung chung như 'Vật thể' hay 'Đối tượng'.\n\n" +
                            "Nếu không thể nhận diện, hãy ghi 'Vật thể không xác định' và mô tả ngắn gọn (ví dụ: 'Vật thể không xác định (hộp kim loại màu xanh)').\n\n" +
                            "Trả về một đối tượng JSON có một khóa duy nhất là 'items'. " +
                            "Giá trị của 'items' là một mảng JSON, mỗi phần tử là một vật thể với các trường sau:\n" +
                            "- 'objectName' (String, TÊN CỤ THỂ, ví dụ: 'Ghế sofa ba chỗ bọc nỉ', 'Tủ lạnh Inverter 2 cánh').\n" +
                            "- 'length', 'width', 'height' (Number, cm).\n\n" +
                            "VÍ DỤ KẾT QUẢ MONG MUỐN: \n" +
                            "{\"items\": [{\"length\": 180, \"width\": 80, \"height\": 75, \"objectName\": \"Bàn ăn gỗ công nghiệp\"}," +
                            "{\"length\": 45, \"width\": 50, \"height\": 90, \"objectName\": \"Ghế ăn chân sắt\"}]}";

            String jsonBody = createJsonBody(prompt, base64Image, 2048);
            String responseContent = sendOpenAiRequest(jsonBody);

            logger.info("Phản hồi từ OpenAI: {}", responseContent);

            JsonNode rootNode = objectMapper.readTree(responseContent);
            JsonNode choices = rootNode.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode messageNode = firstChoice.get("message");
                if (messageNode != null && messageNode.has("content")) {
                    String jsonContent = messageNode.get("content").asText();
                    try {
                        // **THAY ĐỔI 2: Sửa logic đọc kết quả để khớp với cấu trúc {"items": [...]}**
                        FullAnalysisResponse fullResponse = objectMapper.readValue(jsonContent, FullAnalysisResponse.class);

                        // Logic kiểm tra dữ liệu trả về
                        if (fullResponse == null || fullResponse.getItems() == null || fullResponse.getItems().isEmpty()) {
                            logger.warn("AI trả về danh sách items rỗng hoặc null. Kích hoạt phương án dự phòng.");
                            return createSampleMultiObjectData(imageFile.getOriginalFilename());
                        }

                        List<OpenAiApiResponse> parsedResponses = fullResponse.getItems();
                        List<ObjectDimensionsDTO> objectsList = new ArrayList<>();
                        boolean hasMeaningfulName = false;

                        for (OpenAiApiResponse resp : parsedResponses) {
                            if (resp.getObjectName() == null || resp.getObjectName().trim().isEmpty()) {
                                logger.warn("AI trả về một đối tượng không có tên. Bỏ qua đối tượng này.");
                                continue;
                            }

                            objectsList.add(new ObjectDimensionsDTO(
                                    resp.getLength(),
                                    resp.getWidth(),
                                    resp.getHeight(),
                                    resp.getObjectName(),
                                    1.0
                            ));

                            String lowerCaseName = resp.getObjectName().toLowerCase();
                            if (!lowerCaseName.contains("vật thể") && !lowerCaseName.contains("đối tượng")) {
                                hasMeaningfulName = true;
                            }
                        }

                        if (!hasMeaningfulName && !objectsList.isEmpty()) {
                            logger.warn("AI chỉ trả về các tên chung chung. Kích hoạt phương án dự phòng.");
                            return createSampleMultiObjectData(imageFile.getOriginalFilename());
                        }

                        if (objectsList.isEmpty()) {
                            logger.warn("Không có đối tượng hợp lệ nào sau khi lọc. Kích hoạt phương án dự phòng.");
                            return createSampleMultiObjectData(imageFile.getOriginalFilename());
                        }

                        return new ObjectsAnalysisResultDTO(objectsList, objectsList.size());
                    } catch (JsonProcessingException e) {
                        logger.error("Lỗi khi parse JSON từ AI: {}", e.getMessage());
                        return createSampleMultiObjectData(imageFile.getOriginalFilename());
                    }
                }
            }
            logger.error("Phản hồi từ OpenAI không có định dạng mong đợi");
            return createSampleMultiObjectData(imageFile.getOriginalFilename());
        } catch (IOException e) {
            logger.error("Lỗi xử lý hình ảnh đa đối tượng", e);
            return createSampleMultiObjectData(imageFile != null ? imageFile.getOriginalFilename() : "unknown");
        }
    }

    private String createJsonBody(String prompt, String base64Image, int maxTokens) throws JsonProcessingException {
        // **THAY ĐỔI 1: Sửa lỗi khai báo biến trùng lặp**
        ObjectNode requestBody = objectMapper.createObjectNode(); // Khai báo MỘT LẦN DUY NHẤT

        ArrayNode contentArray = objectMapper.createArrayNode();
        ObjectNode textPart = objectMapper.createObjectNode();
        textPart.put("type", "text");
        textPart.put("text", prompt);
        contentArray.add(textPart);

        ObjectNode imagePart = objectMapper.createObjectNode();
        imagePart.put("type", "image_url");
        ObjectNode imageUrl = objectMapper.createObjectNode();
        imageUrl.put("url", base64Image);
        imagePart.set("image_url", imageUrl);
        contentArray.add(imagePart);

        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "user");
        message.set("content", contentArray);

        ArrayNode messagesArray = objectMapper.createArrayNode();
        messagesArray.add(message);

        requestBody.put("model", "gpt-4o");
        requestBody.set("messages", messagesArray);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", 0.2);

        ObjectNode responseFormat = objectMapper.createObjectNode();
        responseFormat.put("type", "json_object");
        requestBody.set("response_format", responseFormat);

        return objectMapper.writeValueAsString(requestBody);
    }

    private String sendOpenAiRequest(String jsonBody) throws IOException {
        RequestBody body = RequestBody.create(MediaType.get("application/json"), jsonBody);
        Request request = new Request.Builder()
                .url(openAiApiUrl)
                .header("Authorization", "Bearer " + openAiApiKey)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                logger.error("Yêu cầu đến OpenAI thất bại: {} {} - {}", response.code(), response.message(), errorBody);
                throw new IOException("Yêu cầu đến OpenAI thất bại: " + response.code());
            }
            return response.body().string();
        }
    }

    private ObjectDimensionsDTO createSampleObjectData(String filename) {
        return new ObjectDimensionsDTO(50.0, 30.0, 20.0, "Vật thể mẫu", 0.8);
    }

    private ObjectsAnalysisResultDTO createSampleMultiObjectData(String filename) {
        List<ObjectDimensionsDTO> objects = new ArrayList<>();
        if (filename != null && filename.toLowerCase().contains("bàn")) {
            objects.add(new ObjectDimensionsDTO(120.0, 60.0, 75.0, "Bàn làm việc (Mẫu)", 0.9));
            objects.add(new ObjectDimensionsDTO(45.0, 45.0, 90.0, "Ghế văn phòng (Mẫu)", 0.85));
        } else {
            objects.add(new ObjectDimensionsDTO(180.0, 85.0, 80.0, "Sofa (Mẫu)", 0.9));
            objects.add(new ObjectDimensionsDTO(50.0, 50.0, 45.0, "Bàn trà (Mẫu)", 0.8));
        }
        return new ObjectsAnalysisResultDTO(objects, objects.size());
    }

    // Lớp nội bộ để parse cấu trúc {"items": [...]}
    private static class FullAnalysisResponse {
        @JsonProperty("items")
        private List<OpenAiApiResponse> items;

        public List<OpenAiApiResponse> getItems() { return items; }
        public void setItems(List<OpenAiApiResponse> items) { this.items = items; }
    }

    // Lớp nội bộ để parse từng đối tượng trong mảng
    private static class OpenAiApiResponse {
        @JsonProperty("length")
        private double length;
        @JsonProperty("width")
        private double width;
        @JsonProperty("height")
        private double height;
        @JsonProperty("objectName")
        private String objectName;

        public double getLength() { return length; }
        public double getWidth() { return width; }
        public double getHeight() { return height; }
        public String getObjectName() { return objectName; }
    }
}