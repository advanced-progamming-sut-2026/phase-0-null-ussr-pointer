package com.ussr.pvz.model.quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestContext {
    public String plantKey;
    public String plantFamily;
    public String chapterId;
    public int columnIndex = -1;
    public int rowIndex = -1;
    public int elapsedSeconds = 0;
    public int waveNumber = 0;
    public boolean hadLawnmower = false;
    public boolean gardenSymmetric = false;
    public boolean gardenAsymmetric = false;
    public int plantsLost = 0;
    public int sunLeft = 0;
    public int sunProducerCount = 0;
    public int explosivesUsed = 0;
    public int consecutiveWins = 0;
    public List<Integer> emptyColumns = new ArrayList<>();
    public List<Integer> emptyRows = new ArrayList<>();

    public Map<String, Object> extra = new HashMap<>();
}