package com.example.celengancerdas;

import java.io.Serializable;

public class SavingsLog implements Serializable {
    private String tanggalTransaksi;
    private String waktuTransaksi;
    private int jumlahTransaksi;


    public SavingsLog(String tanggalTransaksi, String waktuTransaksi, int jumlahTransaksi) {
        this.tanggalTransaksi = tanggalTransaksi;
        this.waktuTransaksi = waktuTransaksi;
        this.jumlahTransaksi = jumlahTransaksi;
    }


    public String getTanggalTransaksi() {
        return tanggalTransaksi;
    }

    public String getWaktuTransaksi() {
        return waktuTransaksi;
    }

    public int getJumlahTransaksi() {
        return jumlahTransaksi;
    }

}
