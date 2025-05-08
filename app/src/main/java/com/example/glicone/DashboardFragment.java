package com.example.glicone;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.Timestamp;
import java.util.ArrayList;

public class DashboardFragment extends Fragment {

    private LineChart lineChartHeartRate;
    private LineChart lineChartGlucosePrediction;
    private FirebaseFirestore firestore;
    private ListenerRegistration heartRateListener;
    private ListenerRegistration glucosePredictionListener;

    public DashboardFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        lineChartHeartRate = view.findViewById(R.id.lineChartHeartRate);
        lineChartGlucosePrediction = view.findViewById(R.id.lineChartGlucosePrediction);

        firestore = FirebaseFirestore.getInstance();

        setupHeartRateChart();
        setupGlucosePredictionChart();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (heartRateListener != null) {
            heartRateListener.remove();
        }
        if (glucosePredictionListener != null) {
            glucosePredictionListener.remove();
        }
    }

    private void setupHeartRateChart() {
        CollectionReference collection = firestore.collection("leituras");

        heartRateListener = collection.orderBy("timestamp", Query.Direction.DESCENDING).limit(10)
                .addSnapshotListener((QuerySnapshot snapshots, FirebaseFirestoreException e) -> {
                    if (e != null || snapshots == null) return;

                    ArrayList<Entry> entries = new ArrayList<>();
                    int index = 0;

                    for (DocumentSnapshot document : snapshots.getDocuments()) {
                        Long bpm = document.getLong("bpm");
                        Timestamp timestamp = document.getTimestamp("timestamp");

                        if (bpm != null && timestamp != null) {
                            entries.add(new Entry(index, bpm.floatValue()));
                            index++;
                        }
                    }

                    LineDataSet dataSet = new LineDataSet(entries, "Intensidade dos Sinais Elétricos (BPM)");
                    dataSet.setColor(getResources().getColor(R.color.teal_700));
                    dataSet.setCircleColor(getResources().getColor(R.color.teal_200));
                    dataSet.setLineWidth(2f);
                    dataSet.setCircleRadius(4f);

                    LineData lineData = new LineData(dataSet);
                    lineChartHeartRate.setData(lineData);
                    lineChartHeartRate.invalidate();
                });
    }

    private void setupGlucosePredictionChart() {
        CollectionReference collection = firestore.collection("leituras");

        glucosePredictionListener = collection.orderBy("timestamp", Query.Direction.DESCENDING).limit(10)
                .addSnapshotListener((QuerySnapshot snapshots, FirebaseFirestoreException e) -> {
                    if (e != null || snapshots == null) return;

                    ArrayList<Entry> entries = new ArrayList<>();
                    int index = 0;

                    for (DocumentSnapshot document : snapshots.getDocuments()) {
                        Double glicemia = document.getDouble("glicemia");
                        Timestamp timestamp = document.getTimestamp("timestamp");

                        if (glicemia != null && timestamp != null) {
                            entries.add(new Entry(index, glicemia.floatValue()));
                            index++;
                        }
                    }

                    LineDataSet dataSet = new LineDataSet(entries, "Previsão de Glicemia");
                    dataSet.setColor(getResources().getColor(R.color.purple_700));
                    dataSet.setCircleColor(getResources().getColor(R.color.purple_200));
                    dataSet.setLineWidth(2f);
                    dataSet.setCircleRadius(4f);

                    LineData lineData = new LineData(dataSet);
                    lineChartGlucosePrediction.setData(lineData);
                    lineChartGlucosePrediction.invalidate();
                });
    }
}