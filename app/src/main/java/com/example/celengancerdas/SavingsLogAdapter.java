package com.example.celengancerdas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class SavingsLogAdapter extends ArrayAdapter<SavingsLog> {
    private Context context;
    private List<SavingsLog> savingsLogsList;

    public SavingsLogAdapter(Context context, List<SavingsLog> savingsLogsList) {
        super(context, 0, savingsLogsList);
        this.context = context;
        this.savingsLogsList = savingsLogsList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.savings_logs_item, parent, false);
        }

        TextView textViewTanggal = convertView.findViewById(R.id.textViewDate);
        TextView textViewWaktu = convertView.findViewById(R.id.textViewWaktu);
        TextView textViewNominal = convertView.findViewById(R.id.textViewNominal);

        SavingsLog savingsLog = getItem(position);

        if (savingsLog != null) {
            textViewTanggal.setText(savingsLog.getTanggalTransaksi());
            textViewWaktu.setText(savingsLog.getWaktuTransaksi());
            textViewNominal.setText(formatRupiah(savingsLog.getJumlahTransaksi()));
            // Tambahkan kode berikut untuk mengubah warna text nominal
            int jumlah = savingsLog.getJumlahTransaksi();
            if (jumlah > 0) {
                // Jika jumlah nominal positif, maka ubah warna menjadi hijau
                textViewNominal.setTextColor(ContextCompat.getColor(context, R.color.green));
            } else {
                // Jika jumlah nominal negatif, maka ubah warna menjadi merah
                textViewNominal.setTextColor(ContextCompat.getColor(context, R.color.red));
            }
        }

        return convertView;
    }

    // Metode untuk mengonversi harga menjadi format mata uang Rupiah (IDR)
    private String formatRupiah(int harga) {
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        return formatRupiah.format((double) harga);
    }


}
