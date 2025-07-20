package org.example.operatormanagementsystem.customer_thai.service.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        try {
            String base64Image = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageFile.getBytes());
            String prompt = "Phân tích hình ảnh này và cung cấp chiều dài, chiều rộng, chiều cao ước tính của đối tượng chính bằng cm. Đồng thời cho biết tên của đối tượng. Trả về kết quả CHÍNH XÁC dưới dạng JSON với các trường: length, width, height, objectName. KHÔNG thêm bất kỳ văn bản giải thích nào.";

            String jsonBody = createJsonBody(prompt, base64Image, 300);
            String responseContent = sendOpenAiRequest(jsonBody);

            JsonNode rootNode = objectMapper.readTree(responseContent);
            JsonNode choices = rootNode.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode messageNode = firstChoice.get("message");
                if (messageNode != null) {
                    JsonNode content = messageNode.get("content");
                    if (content != null && content.isTextual()) {
                        String contentText = content.asText().toLowerCase();
                        if (contentText.contains("cannot analyze") || contentText.contains("cannot interpret") || contentText.contains("i'm sorry") || contentText.contains("i am sorry")) {
                            logger.warn("OpenAI không thể phân tích hình ảnh. Sử dụng dữ liệu mẫu.");
                            return createSampleObjectData(imageFile.getOriginalFilename());
                        }
                        String jsonContent = content.asText();
                        try {
                            OpenAiApiResponse parsedResponse = objectMapper.readValue(jsonContent, OpenAiApiResponse.class);
                            return new ObjectDimensionsDTO(
                                    parsedResponse.getLength(),
                                    parsedResponse.getWidth(),
                                    parsedResponse.getHeight(),
                                    parsedResponse.getObjectName(),
                                    1.0
                            );
                        } catch (JsonProcessingException e) {
                            logger.error("Lỗi khi parse JSON: {}", e.getMessage());
                            return createSampleObjectData(imageFile.getOriginalFilename());
                        }
                    }
                }
            }
            logger.error("Phản hồi từ OpenAI không có định dạng mong đợi");
            return createSampleObjectData(imageFile.getOriginalFilename());
        } catch (IOException e) {
            logger.error("Lỗi xử lý hình ảnh", e);
            throw new RuntimeException("Lỗi xử lý hình ảnh: " + e.getMessage(), e);
        }
    }

    @Override
    public ObjectsAnalysisResultDTO analyzeAllObjectsDimensions(MultipartFile imageFile) {
        try {
            String base64Image = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageFile.getBytes());
            String prompt = "Phân tích hình ảnh này và cung cấp chiều dài, chiều rộng, chiều cao ước tính của TẤT CẢ các đối tượng có trong ảnh bằng cm. Trả về kết quả CHÍNH XÁC dưới dạng JSON với định dạng mảng các đối tượng, mỗi đối tượng có các trường: length, width, height, objectName. VÍ DỤ: [{\"length\": 10, \"width\": 5, \"height\": 3, \"objectName\": \"Bàn\"}, {\"length\": 2, \"width\": 2, \"height\": 5, \"objectName\": \"Chai nước\"}]";

            String jsonBody = createJsonBody(prompt, base64Image, 1000);
            String responseContent = sendOpenAiRequest(jsonBody);

            JsonNode rootNode = objectMapper.readTree(responseContent);
            JsonNode choices = rootNode.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode messageNode = firstChoice.get("message");
                if (messageNode != null) {
                    JsonNode content = messageNode.get("content");
                    if (content != null && content.isTextual()) {
                        String contentText = content.asText().toLowerCase();
                        if (contentText.contains("cannot analyze") || contentText.contains("cannot interpret") || contentText.contains("i'm sorry") || contentText.contains("i am sorry")) {
                            logger.warn("OpenAI không thể phân tích hình ảnh. Sử dụng dữ liệu mẫu.");
                            return createSampleMultiObjectData(imageFile.getOriginalFilename());
                        }
                        String jsonContent = content.asText();
                        try {
                            List<OpenAiApiResponse> parsedResponses = objectMapper.readValue(jsonContent, new TypeReference<List<OpenAiApiResponse>>() {});
                            List<ObjectDimensionsDTO> objectsList = new ArrayList<>();
                            for (OpenAiApiResponse resp : parsedResponses) {
                                objectsList.add(new ObjectDimensionsDTO(
                                        resp.getLength(),
                                        resp.getWidth(),
                                        resp.getHeight(),
                                        resp.getObjectName(),
                                        1.0
                                ));
                            }
                            return new ObjectsAnalysisResultDTO(objectsList, objectsList.size());
                        } catch (JsonProcessingException e) {
                            logger.error("Lỗi khi parse JSON: {}", e.getMessage());
                            return createSampleMultiObjectData(imageFile.getOriginalFilename());
                        }
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

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", "gpt-4o");
        requestBody.set("messages", messagesArray);
        requestBody.put("max_tokens", maxTokens);

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
                throw new IOException("Yêu cầu đến OpenAI thất bại: " + response.code() + " " + response.message());
            }
            return response.body().string();
        }
    }

    private ObjectDimensionsDTO createSampleObjectData(String filename) {
        if (filename == null) {
            filename = "unknown";
        }

        String objectName = "Unknown Object";
        double length = 50.0;
        double width = 30.0;
        double height = 20.0;

        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.contains("ban") || lowerFilename.contains("table")) {
            objectName = "Bàn";
            length = 120.0;
            width = 60.0;
            height = 75.0;
        } else if (lowerFilename.contains("ghe") || lowerFilename.contains("chair")) {
            objectName = "Ghế";
            length = 45.0;
            width = 45.0;
            height = 90.0;
        } else if (lowerFilename.contains("tu") || lowerFilename.contains("cabinet")) {
            objectName = "Tủ";
            length = 80.0;
            width = 40.0;
            height = 180.0;
        }

        return new ObjectDimensionsDTO(length, width, height, objectName, 0.8);
    }

    private ObjectsAnalysisResultDTO createSampleMultiObjectData(String filename) {
        if (filename == null) {
            filename = "unknown";
        }

        List<ObjectDimensionsDTO> objects = new ArrayList<>();
        String lowerFilename = filename.toLowerCase();

        if (lowerFilename.contains("ban") || lowerFilename.contains("table")) {
            objects.add(new ObjectDimensionsDTO(120.0, 60.0, 75.0, "Bàn làm việc", 0.9));
            objects.add(new ObjectDimensionsDTO(45.0, 45.0, 90.0, "Ghế văn phòng", 0.85));
            objects.add(new ObjectDimensionsDTO(30.0, 20.0, 25.0, "Đèn bàn", 0.75));
            objects.add(new ObjectDimensionsDTO(25.0, 20.0, 5.0, "Sách", 0.7));
        } else if (lowerFilename.contains("ghe") || lowerFilename.contains("chair")) {
            objects.add(new ObjectDimensionsDTO(45.0, 45.0, 90.0, "Ghế văn phòng", 0.9));
            objects.add(new ObjectDimensionsDTO(40.0, 40.0, 45.0, "Ghế đẩu", 0.85));
        } else if (lowerFilename.contains("tu") || lowerFilename.contains("cabinet")) {
            objects.add(new ObjectDimensionsDTO(80.0, 40.0, 180.0, "Tủ quần áo", 0.9));
            objects.add(new ObjectDimensionsDTO(60.0, 35.0, 120.0, "Kệ sách", 0.85));
        } else {
            objects.add(new ObjectDimensionsDTO(50.0, 30.0, 20.0, "Vật thể 1", 0.7));
            objects.add(new ObjectDimensionsDTO(25.0, 25.0, 40.0, "Vật thể 2", 0.7));
            objects.add(new ObjectDimensionsDTO(100.0, 50.0, 30.0, "Vật thể 3", 0.7));
        }

        return new ObjectsAnalysisResultDTO(objects, objects.size());
    }

    private static class OpenAiApiResponse {
        @JsonProperty("length")
        private double length;
        @JsonProperty("width")
        private double width;
        @JsonProperty("height")
        private double height;
        @JsonProperty("objectName")
        private String objectName;

        public double getLength() {
            return length;
        }

        public double getWidth() {
            return width;
        }

        public double getHeight() {
            return height;
        }

        public String getObjectName() {
            return objectName;
        }
    }
}