package com.example.dogtodo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String PREFS = "dog_todo_store";
    private final List<Task> tasks = new ArrayList<>();
    private SharedPreferences prefs;
    private LinearLayout taskList;
    private TextView emptyState;
    private TextView progressCount;
    private ProgressBar progressBar;
    private DogAvatarView headerDog;
    private TextView headerSubtitle;
    private LinearLayout dogPicker;
    private EditText taskInput;
    private int selectedDog = 0;

    private static class Task {
        String title;
        boolean done;

        Task(String title, boolean done) {
            this.title = title;
            this.done = done;
        }
    }

    private static class DogBuddy {
        final String name;
        final String description;
        final int type;

        DogBuddy(String name, String description, int type) {
            this.name = name;
            this.description = description;
            this.type = type;
        }
    }

    private static final DogBuddy[] DOGS = {
            new DogBuddy("학생 강아지", "차근차근 한 걸음", 0),
            new DogBuddy("의사 강아지", "건강한 하루", 1),
            new DogBuddy("운동 강아지", "몸을 깨우는 시간", 2),
            new DogBuddy("요리사 강아지", "맛있는 루틴", 3),
            new DogBuddy("정리왕 강아지", "깔끔하게 정리", 4),
            new DogBuddy("예술가 강아지", "나만의 속도로", 5)
    };

    public static String dogName(int type) {
        if (type >= 0 && type < DOGS.length) return DOGS[type].name;
        return DOGS[0].name;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(color(R.color.app_background));
        getWindow().setNavigationBarColor(color(R.color.app_background));
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        selectedDog = prefs.getInt("selected_dog", 0);
        loadTasks();
        buildScreen();
    }

    private void loadTasks() {
        String today = todayKey();
        if (!today.equals(prefs.getString("today", ""))) {
            tasks.clear();
            prefs.edit().putString("today", today).putString("tasks", "[]").apply();
            return;
        }
        tasks.clear();
        try {
            JSONArray data = new JSONArray(prefs.getString("tasks", "[]"));
            for (int i = 0; i < data.length(); i++) {
                JSONObject item = data.getJSONObject(i);
                tasks.add(new Task(item.optString("title"), item.optBoolean("done")));
            }
        } catch (Exception ignored) {
            prefs.edit().putString("tasks", "[]").apply();
        }
    }

    private void saveTasks() {
        JSONArray data = new JSONArray();
        for (Task task : tasks) {
            JSONObject item = new JSONObject();
            try {
                item.put("title", task.title);
                item.put("done", task.done);
                data.put(item);
            } catch (Exception ignored) { }
        }
        prefs.edit().putString("today", todayKey()).putString("tasks", data.toString()).apply();
    }

    private String todayKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(new Date());
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(color(R.color.app_background));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(12), dp(16), dp(28));
        scroll.addView(root, new ViewGroup.LayoutParams(-1, -2));

        root.addView(space(8));
        root.addView(buildHeader());
        root.addView(space(16));
        root.addView(buildProgressCard());
        root.addView(space(16));
        root.addView(buildInputCard());
        root.addView(space(24));
        root.addView(label("오늘의 할 일", 18, true));
        root.addView(space(10));

        taskList = new LinearLayout(this);
        taskList.setOrientation(LinearLayout.VERTICAL);
        root.addView(taskList, new LinearLayout.LayoutParams(-1, -2));

        emptyState = label("오늘 할 일이 아직 없어요\n작은 일부터 하나 추가해 볼까요?", 16, false);
        emptyState.setGravity(Gravity.CENTER);
        emptyState.setTextColor(color(R.color.text_secondary));
        emptyState.setBackground(round(color(R.color.surface_soft), 16, color(R.color.border), 1));
        emptyState.setPadding(dp(20), dp(28), dp(20), dp(28));
        LinearLayout.LayoutParams emptyParams = new LinearLayout.LayoutParams(-1, -2);
        emptyParams.topMargin = dp(4);
        root.addView(emptyState, emptyParams);

        root.addView(space(24));
        root.addView(label("오늘 함께할 강아지", 18, true));
        root.addView(space(8));
        root.addView(buildDogPicker());

        setContentView(scroll);
        refreshTaskList();
    }

    private View buildHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(18), dp(16), dp(18), dp(16));
        header.setBackground(round(color(R.color.surface), 18, color(R.color.border), 1));

        headerDog = new DogAvatarView(this);
        headerDog.setDogType(selectedDog);
        header.addView(headerDog, new LinearLayout.LayoutParams(dp(92), dp(92)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(16), 0, 0, 0);
        TextView title = label("오늘도 작은 한 걸음!", 24, true);
        headerSubtitle = label(DOGS[selectedDog].name + "와 함께 시작해요.", 14, false);
        headerSubtitle.setTextColor(color(R.color.text_secondary));
        copy.addView(title);
        copy.addView(space(6));
        copy.addView(headerSubtitle);
        header.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        return header;
    }

    private View buildProgressCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(16));
        card.setBackground(round(color(R.color.surface_soft), 16, Color.TRANSPARENT, 0));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = label("오늘의 진행률", 16, true);
        progressCount = label("0/0 완료", 16, true);
        progressCount.setTextColor(color(R.color.primary));
        top.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        top.addView(progressCount);
        card.addView(top);
        card.addView(space(10));

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(color(R.color.primary)));
        progressBar.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(color(R.color.primary_soft)));
        card.addView(progressBar, new LinearLayout.LayoutParams(-1, dp(8)));
        return card;
    }

    private View buildInputCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(round(color(R.color.surface), 16, color(R.color.border), 1));

        TextView tip = label("작게 나누면 시작하기 쉬워요", 14, true);
        tip.setTextColor(color(R.color.text_secondary));
        card.addView(tip);
        card.addView(space(10));

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        taskInput = new EditText(this);
        taskInput.setSingleLine(true);
        taskInput.setTextSize(16);
        taskInput.setHint("예: 문제 3개 풀기");
        taskInput.setHintTextColor(color(R.color.text_secondary));
        taskInput.setTextColor(color(R.color.text_primary));
        taskInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        taskInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        taskInput.setPadding(dp(14), 0, dp(12), 0);
        taskInput.setBackground(round(color(R.color.app_background), 14, color(R.color.border), 1));
        row.addView(taskInput, new LinearLayout.LayoutParams(0, dp(52), 1));
        Button add = primaryButton("추가");
        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(dp(86), dp(52));
        addParams.leftMargin = dp(8);
        row.addView(add, addParams);
        card.addView(row);

        View.OnClickListener addListener = v -> addTask();
        add.setOnClickListener(addListener);
        taskInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addTask();
                return true;
            }
            return false;
        });
        return card;
    }

    private View buildDogPicker() {
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        dogPicker = new LinearLayout(this);
        dogPicker.setOrientation(LinearLayout.HORIZONTAL);
        scroll.addView(dogPicker, new ViewGroup.LayoutParams(-2, -2));
        refreshDogPicker();
        return scroll;
    }

    private void refreshDogPicker() {
        if (dogPicker == null) return;
        dogPicker.removeAllViews();
        for (int i = 0; i < DOGS.length; i++) {
            final int index = i;
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setGravity(Gravity.CENTER_HORIZONTAL);
            card.setPadding(dp(8), dp(8), dp(8), dp(8));
            card.setBackground(round(index == selectedDog ? color(R.color.primary_soft) : color(R.color.surface),
                    16, index == selectedDog ? color(R.color.primary) : color(R.color.border), index == selectedDog ? 2 : 1));
            DogAvatarView avatar = new DogAvatarView(this);
            avatar.setDogType(i);
            card.addView(avatar, new LinearLayout.LayoutParams(dp(70), dp(70)));
            TextView name = label(DOGS[i].name.replace(" 강아지", ""), 12, index == selectedDog);
            name.setGravity(Gravity.CENTER);
            card.addView(name, new LinearLayout.LayoutParams(dp(86), dp(30)));
            card.setContentDescription(DOGS[i].name + (index == selectedDog ? ", 선택됨" : ", 선택하려면 두 번 탭"));
            card.setOnClickListener(v -> {
                selectedDog = index;
                prefs.edit().putInt("selected_dog", selectedDog).apply();
                headerDog.setDogType(selectedDog);
                headerSubtitle.setText(DOGS[selectedDog].name + "와 함께 시작해요.");
                refreshDogPicker();
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(102), dp(124));
            params.rightMargin = dp(8);
            dogPicker.addView(card, params);
        }
    }

    private void refreshTaskList() {
        if (taskList == null) return;
        taskList.removeAllViews();
        int completed = 0;
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).done) completed++;
            taskList.addView(buildTaskCard(i));
            if (i < tasks.size() - 1) taskList.addView(space(10));
        }
        emptyState.setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
        progressCount.setText(completed + "/" + tasks.size() + " 완료");
        progressBar.setProgress(tasks.isEmpty() ? 0 : Math.round(completed * 100f / tasks.size()));
    }

    private View buildTaskCard(int index) {
        Task task = tasks.get(index);
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(8), dp(8), dp(8), dp(8));
        card.setBackground(round(color(R.color.surface), 16, color(R.color.border), 1));

        CheckBox check = new CheckBox(this);
        check.setButtonTintList(new android.content.res.ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{color(R.color.success), color(R.color.border)}));
        check.setChecked(task.done);
        check.setContentDescription(task.done ? "완료 취소" : "할 일 완료");
        card.addView(check, new LinearLayout.LayoutParams(dp(52), dp(52)));

        LinearLayout textWrap = new LinearLayout(this);
        textWrap.setOrientation(LinearLayout.VERTICAL);
        textWrap.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = label(task.title, 16, true);
        title.setTextColor(task.done ? color(R.color.text_secondary) : color(R.color.text_primary));
        if (task.done) title.setPaintFlags(title.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        TextView status = label(task.done ? "완료했어요" : "오늘 할 일", 12, false);
        status.setTextColor(task.done ? color(R.color.success) : color(R.color.text_secondary));
        textWrap.addView(title);
        textWrap.addView(space(3));
        textWrap.addView(status);
        textWrap.setOnClickListener(v -> showEditDialog(index));
        textWrap.setContentDescription("수정하려면 두 번 탭: " + task.title);
        card.addView(textWrap, new LinearLayout.LayoutParams(0, -2, 1));

        Button more = new Button(this);
        more.setText("⋮");
        more.setTextSize(22);
        more.setTextColor(color(R.color.text_secondary));
        more.setAllCaps(false);
        more.setContentDescription("할 일 메뉴");
        more.setBackgroundColor(Color.TRANSPARENT);
        more.setOnClickListener(v -> showTaskMenu(index));
        card.addView(more, new LinearLayout.LayoutParams(dp(48), dp(52)));

        check.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (task.done == isChecked) return;
            task.done = isChecked;
            saveTasks();
            refreshTaskList();
            if (isChecked) showEncouragement(task.title);
        });
        return card;
    }

    private void addTask() {
        String text = taskInput.getText().toString().trim();
        if (text.isEmpty()) {
            taskInput.setError("할 일 제목을 한 글자 이상 적어 주세요.");
            taskInput.requestFocus();
            return;
        }
        tasks.add(new Task(text, false));
        taskInput.setText("");
        saveTasks();
        refreshTaskList();
        Toast.makeText(this, "오늘의 할 일을 추가했어요.", Toast.LENGTH_SHORT).show();
    }

    private void showTaskMenu(int index) {
        String[] options = {"수정", "삭제"};
        new AlertDialog.Builder(this)
                .setTitle("할 일 메뉴")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showEditDialog(index);
                    else confirmDelete(index);
                }).show();
    }

    private void showEditDialog(int index) {
        EditText edit = new EditText(this);
        edit.setSingleLine(true);
        edit.setText(tasks.get(index).title);
        edit.setSelectAllOnFocus(true);
        edit.setTextSize(16);
        edit.setPadding(dp(16), 0, dp(16), 0);
        edit.setBackground(round(color(R.color.app_background), 14, color(R.color.border), 1));
        LinearLayout wrap = new LinearLayout(this);
        wrap.setPadding(dp(20), 0, dp(20), 0);
        wrap.addView(edit, new LinearLayout.LayoutParams(-1, dp(52)));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("할 일 수정")
                .setView(wrap)
                .setNegativeButton("취소", null)
                .setPositiveButton("저장", null)
                .create();
        dialog.setOnShowListener(v -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String value = edit.getText().toString().trim();
            if (value.isEmpty()) {
                edit.setError("내용을 입력해 주세요.");
                return;
            }
            tasks.get(index).title = value;
            saveTasks();
            refreshTaskList();
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void confirmDelete(int index) {
        new AlertDialog.Builder(this)
                .setTitle("할 일을 삭제할까요?")
                .setMessage("삭제한 내용은 오늘 목록에서 사라져요.")
                .setNegativeButton("취소", null)
                .setPositiveButton("삭제", (dialog, which) -> {
                    tasks.remove(index);
                    saveTasks();
                    refreshTaskList();
                }).show();
    }

    private void showEncouragement(String title) {
        int type = dogTypeFor(title);
        String[] messages = messagesFor(type);
        final int[] messageIndex = {0};

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(22), dp(6), dp(22), 0);
        DogAvatarView avatar = new DogAvatarView(this);
        avatar.setDogType(type);
        content.addView(avatar, new LinearLayout.LayoutParams(dp(136), dp(136)));
        TextView completed = label("‘" + title + "’ 완료!", 18, true);
        completed.setGravity(Gravity.CENTER);
        content.addView(completed);
        content.addView(space(8));
        TextView message = label(messages[0], 20, true);
        message.setGravity(Gravity.CENTER);
        message.setTextColor(color(R.color.primary));
        content.addView(message);
        content.addView(space(10));
        TextView note = label("이 응원은 일상적인 동기 부여를 위한 메시지예요.", 12, false);
        note.setGravity(Gravity.CENTER);
        note.setTextColor(color(R.color.text_secondary));
        content.addView(note);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(content)
                .setNeutralButton("다른 응원 보기", null)
                .setPositiveButton("확인", null)
                .create();
        dialog.setOnShowListener(v -> dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(view -> {
            messageIndex[0] = (messageIndex[0] + 1) % messages.length;
            message.setText(messages[messageIndex[0]]);
        }));
        dialog.show();
    }

    private int dogTypeFor(String title) {
        String text = title.toLowerCase(Locale.KOREA);
        if (containsAny(text, "운동", "걷기", "달리기", "스트레칭", "헬스", "exercise", "run")) return 2;
        if (containsAny(text, "정리", "청소", "설거지", "빨래", "방", "clean")) return 4;
        if (containsAny(text, "요리", "밥", "식사", "도시락", "cook")) return 3;
        if (containsAny(text, "휴식", "잠", "물", "건강", "병원")) return 1;
        if (containsAny(text, "그림", "글쓰기", "음악", "만들", "draw", "write")) return 5;
        return selectedDog == 0 ? 0 : selectedDog;
    }

    private boolean containsAny(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }

    private String[] messagesFor(int type) {
        switch (type) {
            case 1: return new String[]{"몸과 마음을 잘 챙겼어!", "천천히 해도 충분히 잘하고 있어."};
            case 2: return new String[]{"몸을 움직인 멋진 한 걸음이야!", "오늘의 에너지를 잘 채웠어."};
            case 3: return new String[]{"정성 가득한 일을 해냈어!", "맛있는 루틴을 하나 완성했네."};
            case 4: return new String[]{"깔끔하게 하나 끝냈어!", "작은 정리가 큰 편안함이 됐어."};
            case 5: return new String[]{"나만의 속도로 멋지게 해냈어!", "생각을 행동으로 옮긴 게 정말 좋아."};
            default: return new String[]{"오늘도 한 걸음 성장했어!", "작게 시작해서 멋지게 끝냈네."};
        }
    }

    private TextView label(String text, int sizeSp, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(sizeSp);
        view.setTextColor(color(R.color.text_primary));
        view.setTypeface(android.graphics.Typeface.create("sans", bold ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL));
        view.setGravity(Gravity.CENTER_VERTICAL);
        return view;
    }

    private Button primaryButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(16);
        button.setTextColor(color(R.color.on_primary));
        button.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        button.setAllCaps(false);
        button.setBackground(round(color(R.color.primary), 14, Color.TRANSPARENT, 0));
        return button;
    }

    private TextView space(int dp) {
        TextView view = new TextView(this);
        view.setLayoutParams(new LinearLayout.LayoutParams(1, dp(dp)));
        return view;
    }

    private GradientDrawable round(int fill, int radius, int stroke, int strokeWidth) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radius));
        if (strokeWidth > 0) drawable.setStroke(dp(strokeWidth), stroke);
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private int color(int id) {
        return getResources().getColor(id, getTheme());
    }
}
