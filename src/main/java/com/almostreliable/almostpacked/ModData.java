package com.almostreliable.almostpacked;

class ModData {

    AddonFile installedFile;

    @Override
    public int hashCode() {
        return installedFile.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ModData o && installedFile.equals(o.installedFile);
    }

    @Override
    public String toString() {
        return installedFile == null ? "NO FILE" : installedFile.toString();
    }

    @SuppressWarnings({"unused", "FieldNamingConvention", "java:S116"})
    public static class AddonFile {

        private int id;
        private String downloadUrl;
        private String fileName;
        private int projectId;
        private String FileNameOnDisk;

        @Override
        public int hashCode() {
            var result = id;
            result = 31 * result + downloadUrl.hashCode();
            result = 31 * result + getFileName().hashCode();
            result = 31 * result + projectId;
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            var addonFile = (AddonFile) o;
            return id == addonFile.id && projectId == addonFile.projectId &&
                downloadUrl.equals(addonFile.downloadUrl) &&
                getFileName().equals(addonFile.getFileName());
        }

        @Override
        public String toString() {
            return getFileName();
        }

        String getDownloadUrl() {
            return downloadUrl.replace(" ", "%20");
        }

        String getFileName() {
            return fileName != null ? fileName : FileNameOnDisk;
        }

        int getProjectId() {
            return projectId;
        }
    }
}
