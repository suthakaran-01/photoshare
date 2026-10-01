package com.photoshare.dto;

import java.util.List;

public record UploadResult(List<PhotoResponse> uploaded, List<String> failed) {}