package com.marc.helmet.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.marc.helmet.R;
import com.marc.helmet.models.Device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scanned Pico devices. Layout {@code item_device}.
 */
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    public interface DeviceAdapterListener {

        void onConnect(Device device);

        void onPing(Device device);
    }

    private final List<Device> devices = new ArrayList<>();
    private final Map<String, Long> pingMsByDeviceType = new HashMap<>();
    private final DeviceAdapterListener listener;

    public DeviceAdapter(List<Device> devices, DeviceAdapterListener listener) {
        this.listener = listener;
        if (devices != null) {
            this.devices.addAll(devices);
        }
    }

    public void updateDevices(List<Device> incoming) {
        devices.clear();
        pingMsByDeviceType.clear();
        if (incoming != null) {
            devices.addAll(incoming);
        }
        notifyDataSetChanged();
    }

    public void updatePingResult(String deviceType, long ms) {
        if (deviceType == null) {
            return;
        }
        pingMsByDeviceType.put(deviceType, ms);
        for (int i = 0; i < devices.size(); i++) {
            String t = devices.get(i).getDeviceType();
            if (deviceType.equals(t)) {
                notifyItemChanged(i);
                return;
            }
        }
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Device d = devices.get(position);
        boolean helmet = d.isHelmet();
        boolean bike = Device.BIKE.equals(d.getDeviceType());
        holder.tvIcon.setText(helmet ? "H" : (bike ? "B" : "?"));
        holder.tvType.setText(helmet ? "HELMET UNIT" : (bike ? "BIKE UNIT" : "DEVICE"));

        String ver = d.getFirmwareVersion();
        if (ver == null || ver.isEmpty()) {
            ver = "—";
        }
        holder.tvIp.setText(d.getIpAddress() + " · v" + ver);

        Long ping = pingMsByDeviceType.get(d.getDeviceType());
        if (ping != null) {
            holder.tvPingMs.setText(ping + " ms");
        } else {
            holder.tvPingMs.setText("— ms");
        }

        if (d.isConnected()) {
            holder.btnConnect.setText("DISCONNECT");
            holder.btnConnect.setBackgroundResource(R.drawable.bg_button_red);
            holder.btnConnect.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
        } else {
            holder.btnConnect.setText("CONNECT");
            holder.btnConnect.setBackgroundResource(R.drawable.bg_button_outline_red);
            holder.btnConnect.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimary));
        }

        holder.btnConnect.setOnClickListener(
                v -> {
                    int pos = holder.getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onConnect(devices.get(pos));
                    }
                });

        holder.tvPingMs.setOnClickListener(
                v -> {
                    int pos = holder.getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onPing(devices.get(pos));
                    }
                });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    static final class DeviceViewHolder extends RecyclerView.ViewHolder {

        final TextView tvIcon;
        final TextView tvType;
        final TextView tvIp;
        final TextView tvPingMs;
        final Button btnConnect;

        DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tv_device_icon);
            tvType = itemView.findViewById(R.id.tv_device_type);
            tvIp = itemView.findViewById(R.id.tv_device_ip);
            tvPingMs = itemView.findViewById(R.id.tv_ping_ms);
            btnConnect = itemView.findViewById(R.id.btn_connect);
        }
    }
}
