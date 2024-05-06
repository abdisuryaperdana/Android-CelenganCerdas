package com.example.celengancerdas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CelenganAdapter extends BaseAdapter {
    private Context context;
    private List<DashboardActivity.Celengan> celenganList;
    private DatabaseHelper databaseHelper; // Add DatabaseHelper instance


    public CelenganAdapter(Context context, List<DashboardActivity.Celengan> celenganList, DatabaseHelper databaseHelper) {
        this.context = context;
        this.celenganList = celenganList;
        this.databaseHelper = databaseHelper;
    }

    // Tambahkan metode untuk memperbarui data pada adapter
    public void updateData(List<DashboardActivity.Celengan> updatedList) {
        celenganList.clear();
        celenganList.addAll(updatedList);
        notifyDataSetChanged(); // Memperbarui tampilan setelah mengubah data
    }

    @Override
    public int getCount() {
        return celenganList.size();
    }

    @Override
    public Object getItem(int position) {
        return celenganList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.list_celengan_layout, parent, false);

            holder.imageViewCelengan = convertView.findViewById(R.id.imageViewCelengan);
            holder.textViewNama = convertView.findViewById(R.id.textViewNamaCelengan);
            holder.textViewHarga = convertView.findViewById(R.id.textViewHargaCelengan);
            holder.textViewTerkumpul = convertView.findViewById(R.id.textViewJumlahTerkumpul); // inisialisasi textViewTerkumpul

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DashboardActivity.Celengan celengan = celenganList.get(position);

        byte[] imageByteArray = celengan.getImageBlob();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
        holder.imageViewCelengan.setImageBitmap(bitmap);

        holder.textViewNama.setText(celengan.getNamaCelengan());
        int hargaCelengan = celengan.getHargaCelengan();
        String formattedHarga = formatRupiah(hargaCelengan);
        holder.textViewHarga.setText(formattedHarga);

//        int jumlahTerkumpul = celengan.getJumlahTerkumpul();

        int celenganId = celengan.getId(); // Sesuaikan dengan nama metode yang memberikan ID celengan
        int lastTransactionAmount = databaseHelper.getLastTransactionAmountByCelenganId(celenganId);
        String terkumpulText = "Terkumpul: " + formatRupiah(lastTransactionAmount);
        holder.textViewTerkumpul.setText(terkumpulText);

        return convertView;
    }


    // Metode untuk mengonversi harga menjadi format mata uang Rupiah (IDR)
    private String formatRupiah(int harga) {
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        return formatRupiah.format((double) harga);
    }

    static class ViewHolder {
        ImageView imageViewCelengan;
        TextView textViewNama;
        TextView textViewHarga;
        TextView textViewTerkumpul;
    }
}
