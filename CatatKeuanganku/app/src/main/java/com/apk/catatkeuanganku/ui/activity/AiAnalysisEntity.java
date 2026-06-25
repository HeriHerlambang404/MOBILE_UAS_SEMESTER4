package com.apk.catatkeuanganku.ui.activity;


public class AiAnalysisEntity {
    @Entity(tableName = "ai_analysis")
    public class AiAnalysisEntity {
        @PrimaryKey(autoGenerate = true)
        public int id;
        public String analysisResult;
        public String date; // Format: YYYY-MM-DD
    }
}
