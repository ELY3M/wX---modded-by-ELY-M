    public void SubmitSpotterNetworkReport() {
        sendToast("Note that the following form will be directed to the National Weather Service.  Willful false reports are punishable by law.");
        float lat = 0.0f;
        float lon = 0.0f;
        if (this.mMyLocation != null) {
            lat = (float) this.mMyLocation.getLatitude();
            lon = (float) this.mMyLocation.getLongitude();
        }
        int gps = 0;
        if (this.forceGPS) {
            gps = 1;
        }
        ShowUrl("https://www.spotternetwork.org/sreport_m.php?lat=" + lat + "&lon=" + lon + "&gps=" + gps + "&id=" + this.key_spotternetwork);
    }