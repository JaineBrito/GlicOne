package com.example.glicone;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
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

            for (DocumentSnapshot document : snapshots.getDocuments()) {
                Long bpm = document.getLong("bpm");
                Double glicemia = document.getDouble("glicemia");

                if (bpm != null) {
                    heartRateEntries.add(new Entry(index, bpm.floatValue()));
                }
                if (glicemia != null) {
                    glucoseEntries.add(new Entry(index, glicemia.floatValue()));
                }
                index++;
            }

            LineDataSet heartRateDataSet = new LineDataSet(heartRateEntries, "Intensidade dos Sinais Elétricos (BPM)");
            heartRateDataSet.setColor(getResources().getColor(R.color.teal_700));
            heartRateDataSet.setLineWidth(2f);
            heartRateDataSet.setDrawCircles(false);
            heartRateDataSet.setDrawValues(false);
            heartRateDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            heartRateDataSet.setCubicIntensity(0.2f);

            LineData heartRateLineData = new LineData(heartRateDataSet);
            lineChartHeartRate.setData(heartRateLineData);

            lineChartHeartRate.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            lineChartHeartRate.getXAxis().setDrawGridLines(false);
            lineChartHeartRate.getXAxis().setDrawLabels(false);
            lineChartHeartRate.getAxisLeft().setDrawGridLines(true);
            lineChartHeartRate.getAxisRight().setEnabled(false);
            lineChartHeartRate.getDescription().setEnabled(false);
            lineChartHeartRate.setDrawGridBackground(false);
            lineChartHeartRate.invalidate();

            LineDataSet glucoseDataSet = new LineDataSet(glucoseEntries, "Previsão de Glicemia");
            glucoseDataSet.setColor(getResources().getColor(R.color.purple_700));
            glucoseDataSet.setLineWidth(2f);
            glucoseDataSet.setDrawCircles(false);
            glucoseDataSet.setDrawValues(false);
            glucoseDataSet.setMode(LineDataSet.Mode.LINEAR);

            LineData glucoseLineData = new LineData(glucoseDataSet);
            lineChartGlucosePrediction.setData(glucoseLineData);

            lineChartGlucosePrediction.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            lineChartGlucosePrediction.getXAxis().setDrawGridLines(false);
            lineChartGlucosePrediction.getXAxis().setDrawLabels(false);
            lineChartGlucosePrediction.getAxisLeft().setDrawGridLines(true);
            lineChartGlucosePrediction.getAxisRight().setEnabled(false);
            lineChartGlucosePrediction.getDescription().setEnabled(false);
            lineChartGlucosePrediction.setDrawGridBackground(false);
            lineChartGlucosePrediction.invalidate();
        });
    }
}