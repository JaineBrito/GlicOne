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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;

public class DashboardFragment extends Fragment {

    private LineChart lineChartHeartRate;
    private LineChart lineChartGlucosePrediction;
    private FirebaseFirestore firestore;
    private ListenerRegistration dataListener;

    public DashboardFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        lineChartHeartRate = view.findViewById(R.id.lineChartHeartRate);
        lineChartGlucosePrediction = view.findViewById(R.id.lineChartGlucosePrediction);

        lineChartHeartRate.getDescription().setEnabled(false);
        lineChartGlucosePrediction.getDescription().setEnabled(false);

        firestore = FirebaseFirestore.getInstance();
        setupCharts();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dataListener != null) {
            dataListener.remove();
        }
    }

    private void setupCharts() {
        CollectionReference collection = firestore.collection("leituras");
        Query query = collection.orderBy("timestamp", Query.Direction.DESCENDING).limit(20);

        dataListener = query.addSnapshotListener((snapshots, e) -> {
            if (e != null || snapshots == null) return;

            ArrayList<Entry> heartRateEntries = new ArrayList<>();
            ArrayList<Entry> glucoseEntries = new ArrayList<>();
            int index = 0;

            int countValidRecords = 0;

            for (DocumentSnapshot document : snapshots.getDocuments()) {
                if (countValidRecords >= 5) break;

                Long bpm = document.getLong("bpm");
                Double glicemia = document.getDouble("glicemia");

                if (bpm != null && glicemia != null) {
                    heartRateEntries.add(new Entry(index, bpm.floatValue()));
                    glucoseEntries.add(new Entry(index, glicemia.floatValue()));
                    index++;
                    countValidRecords++;
                }
            }

            while (heartRateEntries.size() < 5) {
                heartRateEntries.add(new Entry(index, 0f));
                glucoseEntries.add(new Entry(index, 0f));
                index++;
            }

            LineDataSet heartRateDataSet = new LineDataSet(heartRateEntries, "Intensidade dos Sinais Elétricos (BPM)");
            heartRateDataSet.setColor(getResources().getColor(R.color.teal_700));
            heartRateDataSet.setCircleColor(getResources().getColor(R.color.teal_200));
            heartRateDataSet.setLineWidth(2f);
            heartRateDataSet.setCircleRadius(4f);

            LineData heartRateLineData = new LineData(heartRateDataSet);
            lineChartHeartRate.setData(heartRateLineData);
            lineChartHeartRate.invalidate();

            LineDataSet glucoseDataSet = new LineDataSet(glucoseEntries, "Previsão de Glicemia");
            glucoseDataSet.setColor(getResources().getColor(R.color.purple_700));
            glucoseDataSet.setCircleColor(getResources().getColor(R.color.purple_200));
            glucoseDataSet.setLineWidth(2f);
            glucoseDataSet.setCircleRadius(4f);

            LineData glucoseLineData = new LineData(glucoseDataSet);
            lineChartGlucosePrediction.setData(glucoseLineData);
            lineChartGlucosePrediction.invalidate();
        });
    }
}