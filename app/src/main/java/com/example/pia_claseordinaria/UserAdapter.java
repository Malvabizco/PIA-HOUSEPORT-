package com.example.pia_claseordinaria;

import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private List<String> userIdList;

    public UserAdapter(List<User> userList, List<String> userIdList) {
        this.userList = userList;
        this.userIdList = userIdList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        String userId = userIdList.get(position);

        holder.textViewName.setText(user.fullName != null ? user.fullName : "Sin nombre");
        holder.textViewEmail.setText(user.email != null ? user.email : "Sin correo");
        holder.textViewAddress.setText(user.address != null ? user.address : "Sin dirección");
        holder.textViewPhone.setText(user.phone != null && !user.phone.isEmpty() ? user.phone : "Sin teléfono");

        holder.textViewRoleBadge.setText(user.role != null ? user.role : "USER");
        holder.textViewStatusBadge.setText(user.status != null ? user.status : "pending");

        // Dynamic badge colors for Roles
        int roleColor;
        android.content.Context context = holder.itemView.getContext();
        String roleStr = user.role != null ? user.role : "USER";
        if ("ADMIN".equals(roleStr)) {
            roleColor = context.getResources().getColor(R.color.pastel_blue_txt);
        } else if ("GUARD".equals(roleStr)) {
            roleColor = context.getResources().getColor(R.color.pastel_teal_txt);
        } else {
            roleColor = context.getResources().getColor(R.color.md_theme_light_secondary);
        }
        if (holder.textViewRoleBadge.getBackground() != null) {
            holder.textViewRoleBadge.getBackground().mutate().setTint(roleColor);
        }

        // Dynamic badge colors for Status
        int statusColor;
        String statusStr = user.status != null ? user.status : "pending";
        if ("active".equals(statusStr)) {
            statusColor = context.getResources().getColor(R.color.pastel_green_txt);
        } else if ("pending".equals(statusStr)) {
            statusColor = context.getResources().getColor(R.color.pastel_orange_txt);
        } else {
            statusColor = context.getResources().getColor(R.color.pastel_red_txt);
        }
        if (holder.textViewStatusBadge.getBackground() != null) {
            holder.textViewStatusBadge.getBackground().mutate().setTint(statusColor);
        }

        // Action: Manage User
        holder.buttonActivate.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_user, null);

            TextInputEditText editName = dialogView.findViewById(R.id.editUserFullName);
            TextInputEditText editPhone = dialogView.findViewById(R.id.editUserPhone);
            TextInputEditText editAddress = dialogView.findViewById(R.id.editUserAddress);
            Spinner spinnerRole = dialogView.findViewById(R.id.spinnerUserRole);
            Spinner spinnerStatus = dialogView.findViewById(R.id.spinnerUserStatus);
            Button buttonDelete = dialogView.findViewById(R.id.buttonDeleteUser);

            // Pre-fill values
            editName.setText(user.fullName != null ? user.fullName : "");
            editPhone.setText(user.phone != null ? user.phone : "");
            editAddress.setText(user.address != null ? user.address : "");

            // Setup Role Spinner
            String[] roles = {"USER", "GUARD", "ADMIN"};
            ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, roles);
            roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRole.setAdapter(roleAdapter);
            
            for (int i = 0; i < roles.length; i++) {
                if (roles[i].equals(user.role)) {
                    spinnerRole.setSelection(i);
                    break;
                }
            }

            // Setup Status Spinner
            String[] statuses = {"active", "inactive", "pending"};
            ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, statuses);
            statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerStatus.setAdapter(statusAdapter);

            for (int i = 0; i < statuses.length; i++) {
                if (statuses[i].equals(user.status)) {
                    spinnerStatus.setSelection(i);
                    break;
                }
            }

            // Dialog creation using Material3 Builder
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
            builder.setTitle("Gestionar Usuario")
                   .setView(dialogView)
                   .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                   .setPositiveButton("Guardar", null); // Handled below to prevent auto-close on validation failure

            AlertDialog alertDialog = builder.create();
            final AlertDialog finalDialog = alertDialog;

            // Delete CRUD Operation
            buttonDelete.setOnClickListener(delView -> {
                new MaterialAlertDialogBuilder(context)
                        .setTitle("¿Eliminar Usuario?")
                        .setMessage("¿Estás seguro de que deseas eliminar permanentemente a " + user.fullName + "? Esta acción no se puede deshacer.")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Eliminar", (confirmDialog, confirmWhich) -> {
                            FirebaseFirestore.getInstance().collection("usuarios").document(userId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Usuario eliminado con éxito", Toast.LENGTH_SHORT).show();
                                        int adapterPos = holder.getAdapterPosition();
                                        if (adapterPos != RecyclerView.NO_POSITION && adapterPos < userList.size()) {
                                            userList.remove(adapterPos);
                                            userIdList.remove(adapterPos);
                                            notifyItemRemoved(adapterPos);
                                            notifyItemRangeChanged(adapterPos, userList.size());
                                        }
                                        finalDialog.dismiss();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Error al eliminar usuario: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        })
                        .show();
            });

            alertDialog.show();

            // Override positive button click to validate inputs first
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(saveView -> {
                String newName = editName.getText() != null ? editName.getText().toString().trim() : "";
                String newPhone = editPhone.getText() != null ? editPhone.getText().toString().trim() : "";
                String newAddress = editAddress.getText() != null ? editAddress.getText().toString().trim() : "";
                String newRole = spinnerRole.getSelectedItem().toString();
                String newStatus = spinnerStatus.getSelectedItem().toString();

                if (newName.isEmpty()) {
                    editName.setError("El nombre es requerido");
                    return;
                }
                if (newAddress.isEmpty()) {
                    editAddress.setError("La dirección es requerida");
                    return;
                }

                Map<String, Object> updates = new HashMap<>();
                updates.put("fullName", newName);
                updates.put("phone", newPhone);
                updates.put("address", newAddress);
                updates.put("role", newRole);
                updates.put("status", newStatus);

                FirebaseFirestore.getInstance().collection("usuarios").document(userId)
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Datos de usuario actualizados", Toast.LENGTH_SHORT).show();
                            user.fullName = newName;
                            user.phone = newPhone;
                            user.address = newAddress;
                            user.role = newRole;
                            user.status = newStatus;
                            
                            int adapterPos = holder.getAdapterPosition();
                            if (adapterPos != RecyclerView.NO_POSITION) {
                                notifyItemChanged(adapterPos);
                            }
                            finalDialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            });
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewEmail, textViewAddress, textViewPhone;
        TextView textViewRoleBadge, textViewStatusBadge;
        Button buttonActivate;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewUserName);
            textViewEmail = itemView.findViewById(R.id.textViewUserEmail);
            textViewAddress = itemView.findViewById(R.id.textViewUserAddress);
            textViewPhone = itemView.findViewById(R.id.textViewUserPhone);
            textViewRoleBadge = itemView.findViewById(R.id.textViewUserRoleBadge);
            textViewStatusBadge = itemView.findViewById(R.id.textViewUserStatusBadge);
            buttonActivate = itemView.findViewById(R.id.buttonActivateUser);
        }
    }
}
