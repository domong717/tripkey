package com.example.tripkey.network;

import java.util.List;

public class GptResponse {
    public List<Choice> choices;

    public static class Choice {
        public Message message;
    }

    public static class Message {
        public String role;
        public String content;
    }
}
