package com.example.kinapp.controlador;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kinapp.R;
import com.example.kinapp.modelo.Rutina;
import java.util.List;

public class RutinaAdapter extends RecyclerView.Adapter<RutinaAdapter.ViewHolder> {

    private List<Rutina> listaRutinas;

    public RutinaAdapter(List<Rutina> listaRutinas) {
        this.listaRutinas = listaRutinas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rutina, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Rutina rutina = listaRutinas.get(position);
        holder.txtTitulo.setText(rutina.getTitulo());
        holder.txtDesc.setText(rutina.getDescripcion());
    }

    @Override
    public int getItemCount() {
        return listaRutinas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtTitulo;
        public TextView txtDesc;

        public ViewHolder(View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txt_item_titulo);
            txtDesc = itemView.findViewById(R.id.txt_item_desc);
        }
    }
}