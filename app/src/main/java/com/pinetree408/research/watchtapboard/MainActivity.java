package com.pinetree408.research.watchtapboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SpellCheckerSession.SpellCheckerSessionListener;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class MainActivity extends Activity implements SpellCheckerSessionListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    // Gesture Variable
    private int dragThreshold = 30;
    private final double angleFactor = (double) 180/Math.PI;

    private long touchDownTime;
    private float touchDownX, touchDownY;
    private float scrollY;

    // View Variable
    ArrayList<String> originSourceList;
    ArrayList<String> sourceList;

    ArrayAdapter<String> adapter;
    ListView listview;

    TextView targetView;
    TextView inputView;
    TapBoardView tapBoardView;

    String inputString;
    View keyboardContainer;

    // Task Variable
    Random random;
    String target;
    // 0 : tapboard
    // 1 : result based
    // 2 : input based
    int keyboardMode;
    Toast toast;
    TextView startView;
    View taskView;

    SpellCheckerSession mScs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        random = new Random();
        initSourceList();

        listview = (ListView) findViewById(R.id.list_view);
        targetView = (TextView) findViewById(R.id.target);
        targetView.setTextColor(Color.parseColor("#80000000"));
        inputView = (TextView) findViewById(R.id.input);
        inputView.setTextColor(Color.parseColor("#80000000"));
        tapBoardView = (TapBoardView) findViewById(R.id.tapboard);
        keyboardContainer = findViewById(R.id.keyboard_container);

        startView = (TextView) findViewById(R.id.start);
        taskView = findViewById(R.id.task);

        initListView();
        initStartView();

        initTaskSelectorView();

        final TextServicesManager tsm = (TextServicesManager) getSystemService(
                Context.TEXT_SERVICES_MANAGER_SERVICE);
        mScs = tsm.newSpellCheckerSession(null, null, this, true);
    }

    public void initSourceList() {
        sourceList = new ArrayList<>();
        originSourceList = new ArrayList<>(Arrays.asList(Source.set1));
        for (int i = 0; i < Source.set2.length; i++) {
            if (!originSourceList.contains(Source.set2[i])) {
                originSourceList.add(Source.set2[i]);
            }
        }
        for (int i = 0; i < Source.set3.length; i++) {
            if (!originSourceList.contains(Source.set3[i])) {
                originSourceList.add(Source.set3[i]);
            }
        }
        for (int i = 0; i < Source.set4.length; i++) {
            if (!originSourceList.contains(Source.set4[i])) {
                originSourceList.add(Source.set4[i]);
            }
        }
        Collections.sort(originSourceList);
        sourceList.addAll(originSourceList);
    }

    public void initListView() {
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sourceList){
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view.findViewById(android.R.id.text1);
                tv.setTextColor(Color.BLACK);
                tv.setBackgroundColor(Color.WHITE);
                tv.setGravity(Gravity.CENTER);
                tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                return view;
            }
        };
        listview.setAdapter(adapter);
        listview.setBackgroundColor(Color.parseColor("#d3d3d3"));
    }

    public void initKeyboardContainer() {
        inputString = "";
        keyboardContainer.bringToFront();
        if (keyboardMode != 0) {
            keyboardContainer.setBackgroundColor(Color.WHITE);
        }
        keyboardContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int tempX = (int) event.getAxisValue(MotionEvent.AXIS_X);
                int tempY = (int) event.getAxisValue(MotionEvent.AXIS_Y);
                long eventTime = System.currentTimeMillis();

                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touchDownTime = eventTime;
                        touchDownX = tempX;
                        touchDownY = tempY;
                        scrollY = tempY;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int moveLength = (int) scrollY - tempY;
                        listview.smoothScrollBy(moveLength, 0);
                        scrollY = tempY;
                        break;
                    case MotionEvent.ACTION_UP:
                        long touchTime = eventTime - touchDownTime;
                        int xDir = (int) (touchDownX - tempX);
                        int yDir = (int) (touchDownY - tempY);
                        int len = (int) Math.sqrt(xDir * xDir + yDir * yDir);
                        int speed;
                        if (touchTime > 0) {
                            speed = (int) (len * 1000 / touchTime);
                        } else {
                            speed = 0;
                        }
                        if (len > dragThreshold) {
                            if (speed > 400) {
                                double angle = Math.acos((double) xDir / len) * angleFactor;
                                if (yDir < 0) {
                                    angle = 360 - angle;
                                }
                                angle += 45;
                                int id = (int) (angle / 90);
                                if (id > 3) {
                                    id = 0;
                                }
                                switch (id){
                                    case 0:
                                        //left
                                        if (inputString.length() != 0) {
                                            inputString = inputString.substring(0, inputString.length() - 1);
                                        }
                                        inputView.setText(inputString);
                                        setResultAtListView(inputString);
                                        if (keyboardMode != 0) {
                                            inputView.setVisibility(View.VISIBLE);
                                            tapBoardView.setVisibility(View.VISIBLE);
                                            keyboardContainer.setBackgroundColor(Color.WHITE);
                                        }
                                        break;
                                    case 1:
                                        //top;
                                        break;
                                    case 2:
                                        //right
                                        for (int i = 0; i < listview.getChildCount(); i++) {
                                            TextView childView = (TextView) listview.getChildAt(i);
                                            boolean under = touchDownY <= (childView.getY() + childView.getHeight());
                                            boolean over = touchDownY >= childView.getY();
                                            if (under && over) {
                                                if (childView.getText().equals(target)) {
                                                    target = originSourceList.get(random.nextInt(originSourceList.size()));
                                                    targetView.setText(target);
                                                    startView.setText(target);
                                                    startView.setVisibility(View.VISIBLE);
                                                    taskView.setVisibility(View.GONE);
                                                    inputString = "";
                                                    inputView.setText(inputString);
                                                    setResultAtListView(inputString);
                                                    if (keyboardMode != 0) {
                                                        inputView.setVisibility(View.VISIBLE);
                                                        tapBoardView.setVisibility(View.VISIBLE);
                                                        keyboardContainer.setBackgroundColor(Color.WHITE);
                                                    }
                                                } else {
                                                    childView.setBackgroundColor(Color.parseColor("#f08080"));
                                                }
                                                break;
                                            }
                                        }
                                        break;
                                    case 3:
                                        //bottom;
                                        break;
                                }
                            }
                        } else {
                            if (tapBoardView.getVisibility() == View.VISIBLE) {
                                if (touchTime < 200) {
                                    String[] params = getInputInfo(event);
                                    if (params[0].equals(".")) {
                                        break;
                                    }
                                    inputString += params[0];
                                    inputView.setText(inputString);

                                    mScs.getSentenceSuggestions(new TextInfo[] {new TextInfo(inputString)}, 18);

                                    setResultAtListView(inputString);
                                    if (keyboardMode == 1) {
                                        if (sourceList.size() <= 10) {
                                            if (sourceList.size() != 0) {
                                                inputView.setVisibility(View.GONE);
                                                tapBoardView.setVisibility(View.GONE);
                                                keyboardContainer.setBackgroundColor(Color.parseColor("#00000000"));
                                            } else {
                                                showNoItemsMessage();
                                            }
                                        }
                                    } else if (keyboardMode == 2) {
                                        if (inputString.length() > 2) {
                                            if (sourceList.size() != 0) {
                                                inputView.setVisibility(View.GONE);
                                                tapBoardView.setVisibility(View.GONE);
                                                keyboardContainer.setBackgroundColor(Color.parseColor("#00000000"));
                                            } else {
                                                showNoItemsMessage();
                                            }
                                        }
                                    } else {
                                        if (sourceList.size() == 0) {
                                            showNoItemsMessage();
                                        }
                                    }
                                }
                            }
                        }
                        break;
                }
                return true;
            }
        });
    }

    public void showNoItemsMessage() {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(getApplicationContext(), "There are no itmes", Toast.LENGTH_SHORT);
        toast.show();
    }

    public void initTaskSelectorView() {
        startView.setVisibility(View.GONE);
        taskView.setVisibility(View.GONE);
        final ViewGroup taskSelectorView = (ViewGroup) findViewById(R.id.task_selector);
        for (int i = 0; i < taskSelectorView.getChildCount(); i++) {
            final TextView childView = (TextView) taskSelectorView.getChildAt(i);
            childView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        if (childView.getText().equals("TapBoard")) {
                                            keyboardMode = 0;
                                            inputView.setGravity(Gravity.START|Gravity.CENTER);
                                        } else if (childView.getText().equals("ListBased")) {
                                            keyboardMode = 1;
                                            inputView.setGravity(Gravity.CENTER);
                                        } else {
                                            keyboardMode = 2;
                                            inputView.setGravity(Gravity.CENTER);
                                        }
                                        inputView.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                                        initKeyboardContainer();
                                        startView.setVisibility(View.VISIBLE);
                                        taskSelectorView.setVisibility(View.GONE);
                                    }
                                }
                            );
                            break;
                    }
                    return true;
                }
            });
        }
    }

    public void initStartView() {
        target = originSourceList.get(random.nextInt(originSourceList.size()));
        targetView.setText(target);
        startView.setText(target);
        startView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    startView.setVisibility(View.GONE);
                                    taskView.setVisibility(View.VISIBLE);
                                }
                            }
                        );
                        break;
                }
                return true;
            }
        });
    }

    public void setResultAtListView(String inputString) {
        sourceList.clear();
        if (inputString.equals("")) {
            sourceList.addAll(originSourceList);
        } else {
            ArrayList<String> tempList = new ArrayList<>();
            for (String item : originSourceList) {
                if (item.startsWith(inputString)) {
                    tempList.add(item);
                }
            }
            sourceList.addAll(tempList);
        }
        adapter.notifyDataSetChanged();
        listview.setSelectionAfterHeaderView();
    }

    public String[] getInputInfo(MotionEvent event) {
        double tempX = (double) event.getAxisValue(MotionEvent.AXIS_X);
        double tempY = (double) event.getAxisValue(MotionEvent.AXIS_Y);

        String input = tapBoardView.getKey(tempX, tempY - tapBoardView.getY());

        String[] params = {
                String.valueOf(input),
                String.valueOf(tempX),
                String.valueOf(tempY)
        };
        return params;
    }

    @Override
    public void onGetSuggestions(final SuggestionsInfo[] arg0) {
        Log.d(TAG, "onGetSuggestions");
    }

    @Override
    public void onGetSentenceSuggestions(final SentenceSuggestionsInfo[] arg0) {
        final StringBuilder sb = new StringBuilder();
        for (SentenceSuggestionsInfo ssi : arg0) {
            for (int j = 0; j < ssi.getSuggestionsCount(); ++j) {
                for (int k = 0; k < ssi.getSuggestionsInfoAt(j).getSuggestionsCount(); k++) {
                    if (j != 0) {
                        sb.append(", ");
                    }
                    sb.append(ssi.getSuggestionsInfoAt(j).getSuggestionAt(k));
                }
            }
        }
        Log.d(TAG, sb.toString());
    }
}
