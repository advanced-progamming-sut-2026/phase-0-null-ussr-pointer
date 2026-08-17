package com.ussr.pvz.shared.dto;

public record PickQuestionRequest(
        String questionNumber,
        String answer,
        String answerConfirm
) {
}