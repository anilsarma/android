package com.smartdeviceny.tabbled2;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ScrollView;
import android.widget.Toast;

public class DepartureVisionWebFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.departure_vision_web_fragment, container, false);
        //return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        SwipeRefreshLayout swipeRefreshLayout = getActivity().findViewById(R.id.departure_vision_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SwipeRefreshLayout swipeRefreshLayout = getActivity().findViewById(R.id.departure_vision_layout);
                WebView web = getActivity().findViewById(R.id.nj_map_view_layout);
                web.reload();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        WebView web = getActivity().findViewById(R.id.nj_map_view_layout);
        web.loadUrl("http://dv.njtransit.com/mobile/tid-mobile.aspx?sid=NY");
        web.getSettings().setBuiltInZoomControls(true);
        web.getSettings().setLoadWithOverviewMode(true);
        web.getSettings().setUseWideViewPort(true);

        ScrollView scrollView = getActivity().findViewById(R.id.departure_vision_scroll_view);
        //scrollView.getChildAt();
        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                SwipeRefreshLayout swipeRefreshLayout = getActivity().findViewById(R.id.departure_vision_layout);
                //LinearLayout llParent = findViewById(R.id.departure_vision_scroll);
                swipeRefreshLayout.setEnabled(scrollY ==0); // enable only if at the top.
            }
        });

        super.onViewCreated(view, savedInstanceState);
        //Toast.makeText(getActivity().getApplicationContext(), "OnViewCreated", Toast.LENGTH_LONG).show();

    }
}
