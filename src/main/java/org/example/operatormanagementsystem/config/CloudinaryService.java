package org.example.operatormanagementsystem.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadImage(byte[] fileBytes, String fileName) throws Exception {
        Map<?, ?> result = cloudinary.uploader().upload(fileBytes, ObjectUtils.asMap(
                "public_id", fileName,
                "resource_type", "image"
        ));
        return result.get("secure_url").toString(); // HTTPS URL
    }
}

