package com.example.kinapp.controlador;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kinapp.R;
import com.example.kinapp.modelo.Rutina;
import java.util.List;

/**
 * Este es un adaptador personalizado para gestionar y poblar el RecyclerView del catálogo de ejercicios.
 * Actúa como puente entre la fuente de datos (lista de objetos Rutina)
 * y la interfaz visual (item_rutina.xml), optimizando el reciclaje de vistas en memoria.
 */
public class RutinaAdapter extends RecyclerView.Adapter<RutinaAdapter.ViewHolder> {

    private List<Rutina> listaRutinas;

    /**
     * Inicializa el adaptador con la colección de rutinas a mostrar.
     *
     * @param listaRutinas Lista de objetos de tipo Rutina que contienen la información
     *                     (título y descripción) a renderizar en pantalla.
     */
    public RutinaAdapter(List<Rutina> listaRutinas) {
        this.listaRutinas = listaRutinas;
    }


    /**
     * Esto es invocado automáticamente por el LayoutManager para crear nuevas vistas
     * solo cuando no hay suficientes vistas recicladas disponibles.
     * Infla el archivo XML 'item_rutina.xml' para darle estructura a la fila.
     *
     * @param parent  El ViewGroup al que se añadirá la nueva vista.
     * @param viewType Entero que representa el tipo de vista de la nueva fila.
     * @return ViewHolder Instancia contenedora de las referencias a los TextViews inflados.
     */

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rutina, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Reemplaza o asigna el contenido de los TextViews de una fila específica
     * con los datos de la lista en la posición indicada.
     * También adjunta eventos táctiles al contenedor principal de la fila.
     *
     * @param holder   El contenedor ViewHolder que debe ser actualizado con nuevos datos.
     * @param position Índice del elemento dentro de la lista de rutinas que se está renderizando.
     */

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Rutina rutina = listaRutinas.get(position);
        holder.txtTitulo.setText(rutina.getTitulo());
        holder.txtDesc.setText(rutina.getDescripcion());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Informa al RecyclerView la cantidad total de elementos que debe renderizar.
     *
     * @return int Tamaño actual de la lista de rutinas.
     */

    @Override
    public int getItemCount() {
        return listaRutinas.size();
    }

    /**
     * Clase interna que implementa el patrón ViewHolder para mejorar el rendimiento.
     * Almacena referencias estáticas a las vistas de cada elemento de la lista,
     * evitando llamadas costosas a findViewById() durante el desplazamiento vertical.
     */
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