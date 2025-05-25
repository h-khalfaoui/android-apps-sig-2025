package com.example.quietspaceeee.data.view;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.quietspaceeee.R;
import com.example.quietspaceeee.data.db.ReservationRepository;
import com.example.quietspaceeee.data.model.Reservation;

import java.util.Calendar;

public class CafeReservationActivity extends AppCompatActivity {

    private EditText userNameInput, dateInput, cafeNameInput, numberOfPeopleInput, durationInput;
    String cafeName;
    Calendar calendar;
    private ReservationRepository reservationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        SharedPreferences prefs = getSharedPreferences("QuietSpacePrefs", MODE_PRIVATE);
        String userEmail = prefs.getString("userEmail", null);


        // Activer la flèche retour
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.activity_cafe_reservation);


        cafeName = getIntent().getStringExtra("cafeName");
        reservationRepository = new ReservationRepository(this);

        userNameInput = findViewById(R.id.editUserName);
        dateInput = findViewById(R.id.editDate);
        numberOfPeopleInput = findViewById(R.id.editPeopleCount);
        durationInput = findViewById(R.id.editDuration);

        calendar = Calendar.getInstance();
        dateInput.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePicker = new DatePickerDialog(this,
                    (view, y, m, d) -> dateInput.setText(d + "/" + (m + 1) + "/" + y),
                    year, month, day);
            datePicker.show();
        });

        Button reserveButton = findViewById(R.id.btnConfirmReservation);
        reserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = userNameInput.getText().toString();
                String date = dateInput.getText().toString();

                int numberOfPeople = Integer.parseInt(numberOfPeopleInput.getText().toString());
                int duration = Integer.parseInt(durationInput.getText().toString());


                Reservation reservation = new Reservation(userName, date, cafeName, numberOfPeople, duration, userEmail);
                reservationRepository.insert(reservation);
                Log.d("RES_INSERT", "User Email inseré " + reservation.getUserEmail());
                Toast.makeText(CafeReservationActivity.this, "Réservation enregistrée !", Toast.LENGTH_SHORT).show();
                finish(); // ou afficher un Toast
            }
        });
    }
}
