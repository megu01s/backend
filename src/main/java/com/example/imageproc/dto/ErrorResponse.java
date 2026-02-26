package com.example.imageproc.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ErrorResponse {

    String error;    // e.g. "Bad Request"
    String message;  // e.g. "Invalid image type"
}
