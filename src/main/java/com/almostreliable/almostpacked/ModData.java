package com.almostreliable.almostpacked;

class ModData {

    AddonFile installedFile;

    @Override
    public int hashCode() {
        return installedFile.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ModData modData &&
            modData.installedFile != null &&
            modData.installedFile.equals(installedFile);
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
            result = 31 * result + fileName.hashCode();
            result = 31 * result + projectId;
            result = 31 * result + FileNameOnDisk.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object o) {
            return this == o || o instanceof AddonFile addonFile &&
                id == addonFile.id &&
                downloadUrl.equals(addonFile.downloadUrl) &&
                fileName.equals(addonFile.fileName) &&
                projectId == addonFile.projectId &&
                FileNameOnDisk.equals(addonFile.FileNameOnDisk);
        }

        @Override
        public String toString() {
            return projectId + " | " + FileNameOnDisk;
        }

        String getDownloadUrl() {
            return downloadUrl.replace(" ", "%20");
        }

        String getFileName() {
            return fileName;
        }

        String getFileNameOnDisk() {
            return FileNameOnDisk;
        }

        int getProjectId() {
            return projectId;
        }

        boolean isDisabled() {
            return FileNameOnDisk.endsWith(".disabled");
        }
    }
}
