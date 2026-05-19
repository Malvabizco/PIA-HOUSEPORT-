package com.example.pia_claseordinaria;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pia_claseordinaria.models.Departamento;
import java.util.List;

public class DepartamentoAdapter extends RecyclerView.Adapter<DepartamentoAdapter.ViewHolder> {

    private List<Departamento> departamentos;
    private OnDeptoClickListener listener;

    public interface OnDeptoClickListener {
        void onEdit(Departamento depto);
        void onDelete(Departamento depto);
    }

    public DepartamentoAdapter(List<Departamento> departamentos, OnDeptoClickListener listener) {
        this.departamentos = departamentos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_departamento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Departamento depto = departamentos.get(position);
        holder.textNumero.setText("Depto " + depto.getNumero());
        holder.textTorre.setText("Torre/Sección: " + depto.getTorreSeccion());
        holder.textDueno.setText("Dueño ID: " + depto.getDuenoId());
        
        String estatus = depto.getEstatus().toUpperCase();
        holder.textEstatus.setText(estatus);

        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(8);
        if (estatus.equals("HABITADO")) {
            shape.setColor(Color.parseColor("#4CAF50"));
        } else {
            shape.setColor(Color.parseColor("#F44336"));
        }
        holder.textEstatus.setBackground(shape);

        holder.buttonEdit.setOnClickListener(v -> listener.onEdit(depto));
        holder.buttonDelete.setOnClickListener(v -> listener.onDelete(depto));
    }

    @Override
    public int getItemCount() {
        return departamentos.size();
    }

    public void updateList(List<Departamento> newList) {
        this.departamentos = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textNumero, textTorre, textDueno, textEstatus;
        ImageButton buttonEdit, buttonDelete;

        ViewHolder(View itemView) {
            super(itemView);
            textNumero = itemView.findViewById(R.id.textDeptoNumero);
            textTorre = itemView.findViewById(R.id.textTorreSeccion);
            textDueno = itemView.findViewById(R.id.textDuenoId);
            textEstatus = itemView.findViewById(R.id.textDeptoEstatus);
            buttonEdit = itemView.findViewById(R.id.buttonEditDepto);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteDepto);
        }
    }
}
