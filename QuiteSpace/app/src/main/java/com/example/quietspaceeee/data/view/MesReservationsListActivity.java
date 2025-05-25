package com.example.quietspaceeee.data.view;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.quietspaceeee.R;
import com.example.quietspaceeee.data.db.ReservationRepository;
import com.example.quietspaceeee.data.model.Reservation;

import java.util.ArrayList;
import java.util.List;

public class MesReservationsListActivity extends Activity {

    private ReservationRepository reservationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_list);

        SharedPreferences prefs = getSharedPreferences("QuietSpacePrefs", MODE_PRIVATE);
        String userEmail = prefs.getString("userEmail", null);

        reservationRepository = new ReservationRepository(this);
        ListView listView = findViewById(R.id.reservationListView);

        List<Reservation> reservations = reservationRepository.getReservationsByUserEmail(userEmail); // Filtre
        List<String> displayList = new ArrayList<>();

        for (Reservation r : reservations) {
            displayList.add(r.getUserName() + " - " + r.getCafeName() + " - " + r.getDate()
                    + " - " + r.getNumberOfPeople() + " pers. - " + r.getEstimatedDuration() + "h");
        }

        Log.d("MesReservations", "User Email reçu: " + userEmail);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        listView.setAdapter(adapter);
    }

}
