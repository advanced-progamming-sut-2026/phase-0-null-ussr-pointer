package com.ussr.pvz.model.quest;

import java.util.List;
import java.util.Map;

public class JsonContainer {
    public static class JsonQuestData {
        public String id;
        public String title;
        public String type;
        public String priority;
        public List<JsonCriterionData> criteria;
        public JsonRewardData reward;
        public Long expiresAfterSeconds;
    }

    public static class JsonCriterionData {
        public String type;
        public int target;
        public Map<String, Object> params;
    }

    public static class JsonRewardData {
        public String rewardType;
        public int amount;
        public String formula;
    }
}
