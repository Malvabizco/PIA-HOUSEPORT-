package com.example.pia_claseordinaria;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pia_claseordinaria.data.CondoRepository;
import com.example.pia_claseordinaria.models.Departamento;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.Dispatchers;

public class ManageDepartmentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DepartamentoAdapter adapter;
    private CondoRepository repository;
    private List<Departamento> deptoList = new ArrayList<>();
    private View progressLoading;
    private View textEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_departments);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        repository = new CondoRepository();
        recyclerView = findViewById(R.id.recyclerViewDeptos);
        progressLoading = findViewById(R.id.progressLoading);
        textEmpty = findViewById(R.id.textEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DepartamentoAdapter(deptoList, new DepartamentoAdapter.OnDeptoClickListener() {
            @Override
            public void onEdit(Departamento depto) {
                showDeptoDialog(depto);
            }

            @Override
            public void onDelete(Departamento depto) {
                deleteDepto(depto);
            }
        });
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddDepto);
        fab.setOnClickListener(v -> showDeptoDialog(null));

        loadDepartamentos();
    }

    private void loadDepartamentos() {
        runOnUiThread(() -> {
            progressLoading.setVisibility(View.VISIBLE);
            textEmpty.setVisibility(View.GONE);
        });

        new Thread(() -> {
            try {
                List<Departamento> result = BuildersKt.runBlocking(Dispatchers.getIO(), (scope, continuation) -> 
                    repository.getDepartamentos(continuation)
                );
                
                runOnUiThread(() -> {
                    progressLoading.setVisibility(View.GONE);
                    deptoList = result;
                    adapter.updateList(deptoList);
                    
                    if (deptoList.isEmpty()) {
                        textEmpty.setVisibility(View.VISIBLE);
                    } else {
                        textEmpty.setVisibility(View.GONE);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void showDeptoDialog(Departamento deptoExistente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_departamento, null);
        
        EditText editNumero = view.findViewById(R.id.editDeptoNumero);
        EditText editTorre = view.findViewById(R.id.editTorreSeccion);
        AutoCompleteTextView editDueno = view.findViewById(R.id.editDuenoId);
        Spinner spinnerEstatus = view.findViewById(R.id.spinnerEstatus);

        final List<String> userIds = new ArrayList<>();
        final List<String> userNames = new ArrayList<>();

        // --- Mejora: Seleccionar dueño de la lista de usuarios reales ---
        new Thread(() -> {
            try {
                List<kotlin.Pair<String, String>> users = BuildersKt.runBlocking(Dispatchers.getIO(), (scope, continuation) -> 
                    repository.getAllUsers(continuation)
                );
                for (kotlin.Pair<String, String> user : users) {
                    userIds.add(user.getFirst());
                    userNames.add(user.getSecond());
                }
                runOnUiThread(() -> {
                    ArrayAdapter<String> userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, userNames);
                    editDueno.setAdapter(userAdapter);
                    
                    if (deptoExistente != null) {
                        int index = userIds.indexOf(deptoExistente.getDuenoId());
                        if (index >= 0) {
                            editDueno.setText(userNames.get(index), false);
                        }
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
        // ----------------------------------------------------------------

        String[] estatusOptions = {"vacio", "habitado"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, estatusOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstatus.setAdapter(spinnerAdapter);

        if (deptoExistente != null) {
            builder.setTitle("Editar Departamento");
            editNumero.setText(deptoExistente.getNumero());
            editTorre.setText(deptoExistente.getTorreSeccion());
            // El set de editDueno se hace arriba asíncronamente
            spinnerEstatus.setSelection(deptoExistente.getEstatus().equals("habitado") ? 1 : 0);
        } else {
            builder.setTitle("Nuevo Departamento");
        }

        builder.setView(view);
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String numero = editNumero.getText().toString();
            String torre = editTorre.getText().toString();
            
            String selectedName = editDueno.getText().toString();
            int index = userNames.indexOf(selectedName);
            String dueno = (index >= 0) ? userIds.get(index) : "";

            String estatus = estatusOptions[spinnerEstatus.getSelectedItemPosition()];

            Departamento nuevoDepto = new Departamento(
                deptoExistente != null ? deptoExistente.getId() : "",
                numero, torre, dueno, estatus
            );

            saveDepto(nuevoDepto);
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void saveDepto(Departamento depto) {
        new Thread(() -> {
            try {
                final boolean success;
                if (depto.getId().isEmpty()) {
                    success = BuildersKt.runBlocking(Dispatchers.getIO(), (scope, continuation) -> 
                        repository.createDepartamento(depto, continuation)
                    );
                } else {
                    success = BuildersKt.runBlocking(Dispatchers.getIO(), (scope, continuation) -> 
                        repository.updateDepartamento(depto, continuation)
                    );
                }
                
                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(this, "Guardado correctamente", Toast.LENGTH_SHORT).show();
                        loadDepartamentos();
                    } else {
                        Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Excepción al guardar", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void deleteDepto(Departamento depto) {
        new AlertDialog.Builder(this)
            .setTitle("Eliminar")
            .setMessage("¿Estás seguro de eliminar este departamento?")
            .setPositiveButton("Sí", (dialog, which) -> {
                new Thread(() -> {
                    try {
                        boolean success = BuildersKt.runBlocking(Dispatchers.getIO(), (scope, continuation) -> 
                            repository.deleteDepartamento(depto.getId(), continuation)
                        );
                        runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show();
                                loadDepartamentos();
                            } else {
                                Toast.makeText(this, "Error al eliminar (Permisos denegados)", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(this, "Error de red o permisos", Toast.LENGTH_SHORT).show());
                    }
                }).start();
            })
            .setNegativeButton("No", null)
            .show();
    }
}
