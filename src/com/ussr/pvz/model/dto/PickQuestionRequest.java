package com.ussr.pvz.model.dto;

public record PickQuestionRequest(
        String questionNumber,
        String answer,
        String answerConfirm
) {
}