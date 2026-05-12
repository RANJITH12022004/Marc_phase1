package com.marc.helmet.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.marc.helmet.R;
import com.marc.helmet.models.EmergencyContact;

import java.util.ArrayList;
import java.util.List;

/**
 * Emergency contacts list. Layout {@code item_emergency_contact}.
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    public interface ContactAdapterListener {

        void onEdit(EmergencyContact contact, int position);

        void onDelete(EmergencyContact contact, int position);
    }

    private final List<EmergencyContact> contacts = new ArrayList<>();
    private final ContactAdapterListener listener;

    public ContactAdapter(List<EmergencyContact> contacts, ContactAdapterListener listener) {
        this.listener = listener;
        if (contacts != null) {
            this.contacts.addAll(contacts);
        }
    }

    public void updateContacts(List<EmergencyContact> incoming) {
        contacts.clear();
        if (incoming != null) {
            contacts.addAll(incoming);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_emergency_contact, parent, false);
        return new ContactViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        EmergencyContact c = contacts.get(position);
        int p = c.getPriority();

        holder.tvPriorityNumber.setText(String.valueOf(p));
        if (p == 1) {
            holder.tvPriorityNumber.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.colorSuccess));
        } else {
            holder.tvPriorityNumber.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.textDim));
        }

        holder.tvName.setText(c.getName());
        holder.tvPhone.setText(c.getPhone());
        String rel = c.getRelationship();
        holder.tvRelationship.setText(rel != null ? rel : "");

        if (p == 1) {
            holder.tvPriorityLabel.setText("CALL + SMS");
            holder.tvPriorityLabel.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.colorSuccess));
            holder.tvPriorityLabel.setBackgroundResource(R.drawable.chip_priority_border_green);
        } else {
            holder.tvPriorityLabel.setText("SMS ONLY");
            holder.tvPriorityLabel.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.textDim));
            holder.tvPriorityLabel.setBackgroundResource(R.drawable.chip_priority_border_gray);
        }

        holder.btnEdit.setOnClickListener(
                v -> {
                    int pos = holder.getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onEdit(contacts.get(pos), pos);
                    }
                });
        holder.btnDelete.setOnClickListener(
                v -> {
                    int pos = holder.getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onDelete(contacts.get(pos), pos);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    static final class ContactViewHolder extends RecyclerView.ViewHolder {

        final TextView tvPriorityNumber;
        final TextView tvPriorityLabel;
        final TextView tvName;
        final TextView tvPhone;
        final TextView tvRelationship;
        final ImageButton btnEdit;
        final ImageButton btnDelete;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPriorityNumber = itemView.findViewById(R.id.tv_priority_number);
            tvPriorityLabel = itemView.findViewById(R.id.tv_priority_label);
            tvName = itemView.findViewById(R.id.tv_contact_name);
            tvPhone = itemView.findViewById(R.id.tv_contact_phone);
            tvRelationship = itemView.findViewById(R.id.tv_contact_relationship);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
