package com.marc.helmet.fragments.profile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.marc.helmet.R;
import com.marc.helmet.adapters.ContactAdapter;
import com.marc.helmet.database.DatabaseHelper;
import com.marc.helmet.database.EmergencyContactDao;
import com.marc.helmet.database.UserProfileDao;
import com.marc.helmet.models.EmergencyContact;
import com.marc.helmet.models.UserProfile;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private TextView tvProfileName;
    private TextView tvProfileAge;
    private EditText etName;
    private EditText etAge;
    private Spinner spinnerBloodType;
    private EditText etAllergies;
    private EditText etMedicalConditions;
    private EditText etMedications;
    private EditText etEmergencyNotes;
    private TextView tvContactWarning;

    private UserProfileDao profileDao;
    private EmergencyContactDao contactDao;
    private ContactAdapter adapter;

    private final List<String> bloodTypes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);

        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        profileDao = new UserProfileDao(db);
        contactDao = new EmergencyContactDao(db);

        setupBloodTypeSpinner();
        setupContacts(view);
        loadProfile();
        refreshContacts();

        view.findViewById(R.id.btn_save_profile).setOnClickListener(v -> saveProfile());
        view.findViewById(R.id.btn_add_contact).setOnClickListener(v -> showContactDialog(null));
    }

    private void bindViews(View view) {
        tvProfileName = view.findViewById(R.id.tv_profile_name);
        tvProfileAge = view.findViewById(R.id.tv_profile_age);
        etName = view.findViewById(R.id.et_name);
        etAge = view.findViewById(R.id.et_age);
        spinnerBloodType = view.findViewById(R.id.spinner_blood_type);
        etAllergies = view.findViewById(R.id.et_allergies);
        etMedicalConditions = view.findViewById(R.id.et_medical_conditions);
        etMedications = view.findViewById(R.id.et_medications);
        etEmergencyNotes = view.findViewById(R.id.et_emergency_notes);
        tvContactWarning = view.findViewById(R.id.tv_contact_warning);
    }

    private void setupBloodTypeSpinner() {
        bloodTypes.clear();
        bloodTypes.add("Select...");
        bloodTypes.add("A+");
        bloodTypes.add("A-");
        bloodTypes.add("B+");
        bloodTypes.add("B-");
        bloodTypes.add("AB+");
        bloodTypes.add("AB-");
        bloodTypes.add("O+");
        bloodTypes.add("O-");

        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, bloodTypes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodType.setAdapter(spinnerAdapter);
    }

    private void setupContacts(View root) {
        RecyclerView rvContacts = root.findViewById(R.id.rv_contacts);
        rvContacts.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter =
                new ContactAdapter(
                        contactDao.getAllContacts(),
                        new ContactAdapter.ContactAdapterListener() {
                            @Override
                            public void onEdit(EmergencyContact contact, int position) {
                                showContactDialog(contact);
                            }

                            @Override
                            public void onDelete(EmergencyContact contact, int position) {
                                confirmDeleteContact(contact);
                            }
                        });
        rvContacts.setAdapter(adapter);
    }

    private void loadProfile() {
        UserProfile p = profileDao.getProfile();
        if (p == null) {
            return;
        }
        etName.setText(p.getName());
        etAge.setText(p.getAge() > 0 ? String.valueOf(p.getAge()) : "");
        setSpinnerSelection(p.getBloodType());
        etAllergies.setText(p.getAllergies());
        etMedicalConditions.setText(p.getMedicalConditions());
        etMedications.setText(p.getMedications());
        etEmergencyNotes.setText(p.getEmergencyNotes());
        updateProfileHeader(p.getName(), p.getAge());
    }

    private void setSpinnerSelection(String blood) {
        if (blood == null) {
            spinnerBloodType.setSelection(0);
            return;
        }
        for (int i = 0; i < bloodTypes.size(); i++) {
            if (blood.equalsIgnoreCase(bloodTypes.get(i))) {
                spinnerBloodType.setSelection(i);
                return;
            }
        }
        spinnerBloodType.setSelection(0);
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        if (name.isEmpty()) {
            toast("Name is required");
            return;
        }
        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (Exception e) {
            toast("Enter a valid age");
            return;
        }
        if (age <= 0 || age >= 120) {
            toast("Age must be between 1 and 119");
            return;
        }

        UserProfile existing = profileDao.getProfile();
        UserProfile p = new UserProfile();
        if (existing != null) {
            p.setId(existing.getId());
        }
        p.setName(name);
        p.setAge(age);
        String selectedBlood = (String) spinnerBloodType.getSelectedItem();
        p.setBloodType("Select...".equals(selectedBlood) ? "" : selectedBlood);
        p.setAllergies(etAllergies.getText().toString().trim());
        p.setMedicalConditions(etMedicalConditions.getText().toString().trim());
        p.setMedications(etMedications.getText().toString().trim());
        p.setEmergencyNotes(etEmergencyNotes.getText().toString().trim());
        p.setProfilePhotoPath(existing != null ? existing.getProfilePhotoPath() : "");
        p.setUpdatedAt(System.currentTimeMillis());

        profileDao.insertOrUpdateProfile(p);
        updateProfileHeader(name, age);
        toast("Profile saved. MARC has your back.");
    }

    private void updateProfileHeader(String name, int age) {
        tvProfileName.setText(name != null && !name.isEmpty() ? name : "NO NAME SET");
        tvProfileAge.setText(age > 0 ? ("AGE " + age) : "AGE NOT SET");
    }

    private void refreshContacts() {
        List<EmergencyContact> contacts = contactDao.getAllContacts();
        adapter.updateContacts(contacts);
        tvContactWarning.setVisibility(contacts.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void confirmDeleteContact(EmergencyContact contact) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Remove " + contact.getName() + "?")
                .setMessage("This contact will no longer receive emergency alerts.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton(
                        "Remove",
                        (dialog, which) -> {
                            contactDao.deleteContact(contact.getId());
                            refreshContacts();
                        })
                .show();
    }

    private void showContactDialog(@Nullable EmergencyContact existing) {
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);

        EditText etCName = new EditText(requireContext());
        etCName.setHint("Name");
        root.addView(etCName);

        EditText etPhone = new EditText(requireContext());
        etPhone.setHint("Phone");
        etPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        root.addView(etPhone);

        EditText etRel = new EditText(requireContext());
        etRel.setHint("Relationship");
        root.addView(etRel);

        CheckBox cbPrimary = new CheckBox(requireContext());
        cbPrimary.setText("Make Primary Contact");
        root.addView(cbPrimary);

        if (existing != null) {
            etCName.setText(existing.getName());
            etPhone.setText(existing.getPhone());
            etRel.setText(existing.getRelationship());
            cbPrimary.setChecked(existing.getPriority() == 1);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Add Contact" : "Edit Contact")
                .setView(root)
                .setNegativeButton("CANCEL", null)
                .setPositiveButton(
                        "SAVE CONTACT",
                        (dialog, which) -> {
                            String name = etCName.getText().toString().trim();
                            String phone = etPhone.getText().toString().trim();
                            String relation = etRel.getText().toString().trim();
                            if (name.isEmpty() || phone.isEmpty()) {
                                toast("Name and phone are required");
                                return;
                            }

                            EmergencyContact c = new EmergencyContact();
                            if (existing != null) {
                                c.setId(existing.getId());
                                c.setCreatedAt(existing.getCreatedAt());
                            } else {
                                c.setCreatedAt(System.currentTimeMillis());
                            }
                            c.setName(name);
                            c.setPhone(phone);
                            c.setRelationship(relation);

                            List<EmergencyContact> all = contactDao.getAllContacts();
                            boolean firstContactEver = existing == null && all.isEmpty();
                            int fallbackPriority = all.size() + 1;
                            if (firstContactEver || cbPrimary.isChecked()) {
                                c.setPriority(1);
                            } else {
                                c.setPriority(Math.max(2, fallbackPriority));
                            }

                            if (existing == null) {
                                long rowId = contactDao.insertContact(c);
                                if (rowId > 0L && rowId <= Integer.MAX_VALUE) {
                                    c.setId((int) rowId);
                                }
                            } else {
                                contactDao.updateContact(c);
                            }

                            if (cbPrimary.isChecked()) {
                                List<EmergencyContact> refreshed = contactDao.getAllContacts();
                                List<Integer> order = new ArrayList<>();
                                int primaryId = c.getId();
                                if (primaryId <= 0) {
                                    for (EmergencyContact e : refreshed) {
                                        if (name.equals(e.getName()) && phone.equals(e.getPhone())) {
                                            primaryId = e.getId();
                                            break;
                                        }
                                    }
                                }
                                if (primaryId > 0) {
                                    order.add(primaryId);
                                }
                                for (EmergencyContact e : refreshed) {
                                    if (e.getId() != primaryId) {
                                        order.add(e.getId());
                                    }
                                }
                                contactDao.reorderContacts(order);
                            }

                            refreshContacts();
                        })
                .show();
    }

    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
