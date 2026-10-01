package com.example.kinapp.vista;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.kinapp.R;
import com.example.kinapp.controlador.RutinaAdapter;
import com.example.kinapp.modelo.Rutina;
import java.util.ArrayList;
import java.util.List;

public class RutinasActivity extends AppCompatActivity {

    private RecyclerView recyclerRutinas;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rutinas);

        recyclerRutinas = findViewById(R.id.recycler_rutinas);
        bottomNav = findViewById(R.id.barra_navegacion_rutinas);

        recyclerRutinas.setLayoutManager(new LinearLayoutManager(this));

        List<Rutina> rutinasDePrueba = new ArrayList<>();
        rutinasDePrueba.add(new Rutina("Fortalecimiento de rodilla", "3 ejercicios · 15 min"));
        rutinasDePrueba.add(new Rutina("Rotación de hombro", "4 ejercicios · 10 min"));
        rutinasDePrueba.add(new Rutina("Estiramiento lumbar", "5 ejercicios · 20 min"));
        rutinasDePrueba.add(new Rutina("Rehabilitación de tobillo", "4 ejercicios · 12 min"));

        RutinaAdapter adapter = new RutinaAdapter(rutinasDePrueba);
        recyclerRutinas.setAdapter(adapter);

        bottomNav.setSelectedItemId(R.id.nav_rutinas);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_rutinas) {
                return true;
            } else if (itemId == R.id.nav_inicio) {
                startActivity(new Intent(RutinasActivity.this, PanelActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_progreso) {
                startActivity(new Intent(RutinasActivity.this, ProgresoActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_perfil) {
                startActivity(new Intent(RutinasActivity.this, PerfilActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }
}