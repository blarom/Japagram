package com.japagram.data;

import java.util.List;

public class ConjugationTitle {

    private String title;
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }


    private int titleIndex;
    public int getTitleIndex() {
        return titleIndex;
    }
    public void setTitleIndex(int titleIndex) {
        this.titleIndex = titleIndex;
    }

    private List<Subtitle> subtitles;
    public List<Subtitle> getSubtitles() {
        return subtitles;
    }
    public void setSubtitles(List<Subtitle> subtitles) {
        this.subtitles = subtitles;
    }

    public static class Subtitle {

        private String subtitle;
        public String getTense() {
            return subtitle;
        }
        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        private String ending;
        public String getEnding() {
            return ending;
        }
        public void setEnding(String ending) {
            this.ending = ending;
        }

        private int subtitleIndex;
        public int getSubtitleIndex() {
            return subtitleIndex;
        }
        public void setSubtitleIndex(int subtitleIndex) {
            this.subtitleIndex = subtitleIndex;
        }

    }

}
