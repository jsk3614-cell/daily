import AsyncStorage from '@react-native-async-storage/async-storage';
import { StatusBar } from 'expo-status-bar';
import React, { useEffect, useMemo, useRef, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  Animated,
  Easing,
  Image,
  KeyboardAvoidingView,
  Modal,
  Platform,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';

const STORAGE_KEY = '@dog-todo/state-v1';
const OCCUPATION_SPRITE = require('./assets/dogs-occupations.png');

const COLORS = {
  light: {
    primary: '#C7543F',
    primarySoft: '#FFE1D8',
    background: '#FFF8F3',
    surface: '#FFFFFF',
    surfaceSoft: '#FFF0EA',
    text: '#2B2523',
    muted: '#705F58',
    border: '#E7D8D1',
    success: '#2F8F65',
    error: '#B9383D',
    onPrimary: '#FFFFFF',
  },
  dark: {
    primary: '#F98969',
    primarySoft: '#59352E',
    background: '#1E1816',
    surface: '#2A211E',
    surfaceSoft: '#382925',
    text: '#FFF8F3',
    muted: '#D5C7C2',
    border: '#594741',
    success: '#70D2A4',
    error: '#FF8F8F',
    onPrimary: '#241714',
  },
};

const DOGS = [
  { name: '첫걸음 강아지', short: '첫걸음멍', description: '차근차근 한 걸음', type: 0, kind: 0, col: 0, row: 0, bg: '#FFE1D8' },
  { name: '정장 강아지', short: '정장멍', description: '오늘도 멋지게', type: 1, kind: 0, col: 1, row: 0, bg: '#E2EFFF' },
  { name: '골든 강아지', short: '골든멍', description: '밝고 힘찬 하루', type: 2, kind: 3, col: 2, row: 0, bg: '#FFF1C6' },
  { name: '의사 강아지', short: '의사멍', description: '건강한 하루', type: 3, kind: 1, col: 0, row: 1, bg: '#E2F7EB' },
  { name: '탐험 강아지', short: '탐험멍', description: '새로운 시작', type: 4, kind: 0, col: 1, row: 1, bg: '#FFF1C6' },
  { name: '돌봄 강아지', short: '돌봄멍', description: '주변을 포근하게', type: 5, kind: 4, col: 2, row: 1, bg: '#E8E2FF' },
  { name: '운동 강아지', short: '운동멍', description: '몸을 깨우는 시간', type: 6, kind: 2, col: 0, row: 2, bg: '#FFE2EF' },
  { name: '겨울 강아지', short: '겨울멍', description: '따뜻하게 쉬기', type: 7, kind: 1, col: 1, row: 2, bg: '#E2EFFF' },
  { name: '응원 강아지', short: '응원멍', description: '끝까지 함께하기', type: 8, kind: 5, col: 2, row: 2, bg: '#FFE1D8' },
];

const todayKey = () => {
  const now = new Date();
  const yyyy = now.getFullYear();
  const mm = String(now.getMonth() + 1).padStart(2, '0');
  const dd = String(now.getDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
};

const messageKindFor = (title, fallback) => {
  const text = title.toLowerCase();
  if (['공부', '영어', '회화', '학습', '문제', '시험', '독서', '책', '과제', '수업', 'study', 'english', 'read', 'homework'].some((word) => text.includes(word))) return 6;
  if (['운동', '걷기', '달리기', '스트레칭', '헬스', 'exercise', 'run'].some((word) => text.includes(word))) return 2;
  if (['정리', '청소', '설거지', '빨래', '방', 'clean'].some((word) => text.includes(word))) return 4;
  if (['요리', '밥', '식사', '도시락', 'cook'].some((word) => text.includes(word))) return 3;
  if (['휴식', '잠', '물', '건강', '병원'].some((word) => text.includes(word))) return 1;
  if (['그림', '글쓰기', '음악', '만들', 'draw', 'write'].some((word) => text.includes(word))) return 5;
  return fallback;
};

const messagesForKind = (kind) => {
  const messages = [
    [
      '오늘도 한 걸음 성장했어!',
      '작게 시작해서 멋지게 끝냈네!',
      '해낸 만큼 자신감도 자랐어!',
      '꾸준히 해낸 네가 정말 멋져!',
      '오늘의 작은 완료가 내일을 도와줄 거야!',
      '끝까지 해낸 마음이 반짝반짝 빛나!',
    ],
    [
      '몸과 마음을 잘 챙겼어!',
      '천천히 해도 충분히 잘하고 있어.',
      '오늘의 나를 다정하게 돌봤네!',
      '쉬어갈 줄 아는 것도 멋진 능력이야!',
      '건강한 선택을 하나 더했어!',
      '나를 위한 시간을 잘 지켜냈어!',
    ],
    [
      '몸을 움직인 멋진 한 걸음이야!',
      '오늘의 에너지를 잘 채웠어.',
      '움직일수록 기분도 가벼워져!',
      '작은 운동이 큰 활력이 됐네!',
      '오늘도 힘차게 해냈어!',
      '네 몸이 분명 고마워하고 있을 거야!',
    ],
    [
      '정성 가득한 일을 해냈어!',
      '맛있는 루틴을 하나 완성했네!',
      '차근차근 준비한 마음이 멋져!',
      '오늘도 나를 위한 한 끼를 챙겼어!',
      '정성을 쏟은 만큼 뿌듯함도 커졌어!',
      '든든한 하루를 만드는 데 성공했어!',
    ],
    [
      '깔끔하게 하나 끝냈어!',
      '작은 정리가 큰 편안함이 됐어!',
      '정리한 만큼 마음도 가벼워졌네!',
      '쾌적한 공간을 직접 만들어냈어!',
      '미루지 않고 해낸 게 정말 멋져!',
      '깨끗해진 만큼 기분도 환해졌어!',
    ],
    [
      '나만의 속도로 멋지게 해냈어!',
      '생각을 행동으로 옮긴 게 정말 좋아!',
      '오늘의 아이디어가 멋지게 피어났어!',
      '표현하고 만들어낸 네가 자랑스러워!',
      '한 줄, 한 음절도 소중한 진전이야!',
      '상상한 것을 현실로 한 발짝 옮겼어!',
    ],
    [
      '영어로 한 걸음 더 가까워졌어!',
      '25분 동안 집중한 네가 정말 멋져!',
      '꾸준한 연습이 실력을 키워줘!',
      '오늘도 새로운 표현을 배웠네!',
      '말할수록 자신감이 자라날 거야!',
      '배운 만큼 내일의 네가 더 빛날 거야!',
    ],
  ];
  return messages[kind] || messages[0];
};

function DogAvatar({ type, size = 92, motionKey = 0, animate = false, plainBackground = false, wander = false, wanderDistance = 0 }) {
  const dog = DOGS[type] || DOGS[0];
  const cellHeight = size * (341.333 / 512);
  const motion = useRef(new Animated.Value(0)).current;
  const wanderMotion = useRef(new Animated.Value(0)).current;
  const wanderFacing = useRef(new Animated.Value(1)).current;

  useEffect(() => {
    if (!animate && motionKey === 0) return undefined;
    motion.setValue(0);
    const walkCycle = Animated.sequence([
      Animated.timing(motion, { toValue: 1, duration: 145, easing: Easing.inOut(Easing.ease), useNativeDriver: true }),
      Animated.timing(motion, { toValue: 2, duration: 145, easing: Easing.inOut(Easing.ease), useNativeDriver: true }),
      Animated.timing(motion, { toValue: 3, duration: 145, easing: Easing.inOut(Easing.ease), useNativeDriver: true }),
      Animated.timing(motion, { toValue: 4, duration: 145, easing: Easing.inOut(Easing.ease), useNativeDriver: true }),
      Animated.timing(motion, { toValue: 0, duration: 180, easing: Easing.inOut(Easing.ease), useNativeDriver: true }),
    ]);
    const animation = animate ? Animated.loop(walkCycle) : walkCycle;
    animation.start();
    return () => animation.stop();
  }, [animate, motion, motionKey]);

  useEffect(() => {
    if (!wander) return undefined;
    wanderMotion.setValue(0);
    wanderFacing.setValue(1);
    const animation = Animated.loop(
      Animated.sequence([
        Animated.timing(wanderMotion, { toValue: 1, duration: 1000, easing: Easing.inOut(Easing.ease), useNativeDriver: true }),
        Animated.timing(wanderFacing, { toValue: -1, duration: 1, useNativeDriver: true }),
        Animated.timing(wanderMotion, { toValue: 0, duration: 1000, easing: Easing.inOut(Easing.ease), useNativeDriver: true }),
        Animated.timing(wanderFacing, { toValue: 1, duration: 1, useNativeDriver: true }),
      ]),
    );
    animation.start();
    return () => {
      animation.stop();
      wanderMotion.setValue(0);
      wanderFacing.setValue(1);
    };
  }, [wander, wanderFacing, wanderMotion]);

  const bodyBob = motion.interpolate({
    inputRange: [0, 1, 2, 3, 4],
    outputRange: [0, -0.7, 0.8, -0.7, 0],
  });
  const wanderX = wanderMotion.interpolate({
    inputRange: [0, 1],
    outputRange: [-wanderDistance, wanderDistance],
  });

  return (
    <Animated.View style={{ transform: [{ translateX: wanderDistance ? wanderX : 0 }] }}>
      <Animated.View style={{ transform: [{ scaleX: wanderDistance ? wanderFacing : 1 }] }}>
      <View accessibilityLabel={`${dog.name} 캐릭터`} style={[styles.avatar, { width: size, height: size, borderRadius: size / 2, backgroundColor: plainBackground ? 'transparent' : dog.bg }]}>
      <View style={{ position: 'absolute', width: size, height: cellHeight, top: (size - cellHeight) / 2, overflow: 'hidden' }}>
        <Animated.View style={{ position: 'absolute', width: size, height: cellHeight * 0.72, overflow: 'hidden', transform: [{ translateY: bodyBob }] }}>
          <Image
            source={OCCUPATION_SPRITE}
            resizeMode="stretch"
            style={{
              position: 'absolute',
              width: size * 3,
              height: size * 2,
              left: -dog.col * size,
              top: -dog.row * cellHeight,
            }}
          />
        </Animated.View>
        <View style={{ position: 'absolute', width: size, height: cellHeight * 0.38, bottom: 0, overflow: 'hidden' }}>
          <Image
            source={OCCUPATION_SPRITE}
            resizeMode="stretch"
            style={{ position: 'absolute', width: size * 3, height: size * 2, left: -dog.col * size, top: -(cellHeight * 0.62) - dog.row * cellHeight }}
          />
        </View>
      </View>
      </View>
      </Animated.View>
    </Animated.View>
  );
}

function RoundedButton({ title, onPress, colors, secondary = false, compact = false, accessibilityLabel }) {
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel={accessibilityLabel || title}
      onPress={onPress}
      style={({ pressed }) => [
        styles.button,
        compact && styles.compactButton,
        { backgroundColor: secondary ? colors.surface : colors.primary, borderColor: secondary ? colors.border : colors.primary },
        secondary && styles.secondaryButton,
        pressed && styles.pressed,
      ]}
    >
      <Text style={[styles.buttonText, { color: secondary ? colors.text : colors.onPrimary }]}>{title}</Text>
    </Pressable>
  );
}

export default function App() {
  const scheme = Platform.OS === 'web' ? 'light' : require('react-native').useColorScheme();
  const colors = scheme === 'dark' ? COLORS.dark : COLORS.light;
  const [tasks, setTasks] = useState([]);
  const [selectedDog, setSelectedDog] = useState(0);
  const [input, setInput] = useState('');
  const [ready, setReady] = useState(false);
  const [editIndex, setEditIndex] = useState(null);
  const [editText, setEditText] = useState('');
  const [encouragement, setEncouragement] = useState(null);
  const [messageIndex, setMessageIndex] = useState(0);
  const [completionPulse, setCompletionPulse] = useState(0);

  useEffect(() => {
    (async () => {
      try {
        const stored = await AsyncStorage.getItem(STORAGE_KEY);
        const data = stored ? JSON.parse(stored) : null;
        setSelectedDog(Number.isInteger(data?.selectedDog) ? data.selectedDog : 0);
        setTasks(data?.date === todayKey() && Array.isArray(data.tasks) ? data.tasks : []);
      } catch (error) {
        setTasks([]);
      } finally {
        setReady(true);
      }
    })();
  }, []);

  const save = async (nextTasks = tasks, nextDog = selectedDog) => {
    await AsyncStorage.setItem(STORAGE_KEY, JSON.stringify({ date: todayKey(), tasks: nextTasks, selectedDog: nextDog }));
  };

  const completedCount = useMemo(() => tasks.filter((task) => task.done).length, [tasks]);
  const progress = tasks.length ? Math.round((completedCount / tasks.length) * 100) : 0;
  const currentDog = DOGS[selectedDog] || DOGS[0];

  const addTask = () => {
    const title = input.trim();
    if (!title) return;
    const next = [...tasks, { id: `${Date.now()}`, title, done: false }];
    setTasks(next);
    setInput('');
    save(next);
  };

  const toggleTask = (index) => {
    const task = tasks[index];
    const next = tasks.map((item, itemIndex) => itemIndex === index ? { ...item, done: !item.done } : item);
    setTasks(next);
    save(next);
    if (!task.done) {
      const dogType = selectedDog;
      const messageKind = messageKindFor(task.title, DOGS[selectedDog]?.kind ?? 0);
      setCompletionPulse((value) => value + 1);
      setMessageIndex(0);
      setEncouragement({ title: task.title, dogType, messageKind });
    }
  };

  const confirmDelete = (index) => {
    Alert.alert('할 일을 삭제할까요?', '삭제한 내용은 오늘 목록에서 사라져요.', [
      { text: '취소', style: 'cancel' },
      { text: '삭제', style: 'destructive', onPress: () => {
        const next = tasks.filter((_, itemIndex) => itemIndex !== index);
        setTasks(next);
        save(next);
      } },
    ]);
  };

  const openEdit = (index) => {
    setEditIndex(index);
    setEditText(tasks[index].title);
  };

  const saveEdit = () => {
    const title = editText.trim();
    if (!title || editIndex === null) return;
    const next = tasks.map((task, index) => index === editIndex ? { ...task, title } : task);
    setTasks(next);
    setEditIndex(null);
    save(next);
  };

  const chooseDog = (index) => {
    setSelectedDog(index);
    save(tasks, index);
  };

  if (!ready) {
    return (
      <SafeAreaView style={[styles.safe, { backgroundColor: colors.background }]}>
        <StatusBar style={scheme === 'dark' ? 'light' : 'dark'} />
        <View style={styles.loading}><ActivityIndicator color={colors.primary} /><Text style={[styles.loadingText, { color: colors.muted }]}>할 일을 불러오는 중이에요…</Text></View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={[styles.safe, { backgroundColor: colors.background }]}>
      <StatusBar style={scheme === 'dark' ? 'light' : 'dark'} />
      <KeyboardAvoidingView style={styles.flex} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
        <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
          <View style={[styles.headerCard, { backgroundColor: colors.surface, borderColor: colors.border }]}>
            <View style={[styles.headerDog, { backgroundColor: currentDog.bg }]}>
              <DogAvatar type={selectedDog} size={82} motionKey={completionPulse} plainBackground />
            </View>
            <View style={styles.headerCopy}>
              <Text style={[styles.title, { color: colors.text }]}>꼭해</Text>
              <Text style={[styles.muted, { color: colors.muted }]}>{currentDog.short}과 함께 시작해요.</Text>
            </View>
          </View>

          <View style={[styles.progressCard, { backgroundColor: colors.surfaceSoft }]}>
            <View style={styles.rowBetween}>
              <Text style={[styles.sectionLabel, { color: colors.text }]}>오늘의 진행률</Text>
              <Text style={[styles.count, { color: colors.primary }]}>{completedCount}/{tasks.length} 완료</Text>
            </View>
            <View style={[styles.progressTrack, { backgroundColor: colors.primarySoft }]}>
              <View style={[styles.progressFill, { width: `${progress}%`, backgroundColor: colors.primary }]} />
              {progress < 100 && (
                <View pointerEvents="none" style={[styles.progressDog, { left: `${progress}%` }]}>
                  <DogAvatar type={selectedDog} size={54} plainBackground />
                </View>
              )}
            </View>
          </View>

          <View style={[styles.card, { backgroundColor: colors.surface, borderColor: colors.border }]}>
            <Text style={[styles.mutedBold, { color: colors.muted }]}>작게 나누면 시작하기 쉬워요</Text>
            <View style={styles.inputRow}>
              <TextInput
                accessibilityLabel="오늘 할 일 입력"
                value={input}
                onChangeText={setInput}
                onSubmitEditing={addTask}
                returnKeyType="done"
                placeholder="예: 문제 3개 풀기"
                placeholderTextColor={colors.muted}
                style={[styles.input, { backgroundColor: colors.background, color: colors.text, borderColor: colors.border }]}
              />
              <RoundedButton title="추가" onPress={addTask} colors={colors} compact />
            </View>
          </View>

          <Text style={[styles.sectionTitle, { color: colors.text }]}>오늘의 할 일</Text>
          {tasks.length === 0 ? (
            <View style={[styles.empty, { backgroundColor: colors.surfaceSoft, borderColor: colors.border }]}>
              <DogAvatar type={selectedDog} size={72} />
              <Text style={[styles.emptyTitle, { color: colors.text }]}>오늘 할 일이 아직 없어요</Text>
              <Text style={[styles.muted, { color: colors.muted }]}>작은 일부터 하나 추가해 볼까요?</Text>
            </View>
          ) : (
            tasks.map((task, index) => (
              <View key={task.id || `${task.title}-${index}`} style={[styles.taskCard, { backgroundColor: colors.surface, borderColor: colors.border }]}>
                <Pressable accessibilityRole="checkbox" accessibilityState={{ checked: task.done }} accessibilityLabel={task.done ? '완료 취소' : '할 일 완료'} onPress={() => toggleTask(index)} style={[styles.check, { borderColor: task.done ? colors.success : colors.border, backgroundColor: task.done ? colors.success : 'transparent' }]}>
                  {task.done && <Text style={styles.checkMark}>✓</Text>}
                </Pressable>
                <Pressable onPress={() => openEdit(index)} style={styles.taskCopy} accessibilityLabel={`수정하려면 누르기: ${task.title}`}>
                  <Text style={[styles.taskTitle, { color: task.done ? colors.muted : colors.text, textDecorationLine: task.done ? 'line-through' : 'none' }]}>{task.title}</Text>
                  <Text style={[styles.small, { color: task.done ? colors.success : colors.muted }]}>{task.done ? '완료했어요' : '오늘 할 일'}</Text>
                </Pressable>
                <Pressable accessibilityRole="button" accessibilityLabel="할 일 메뉴" onPress={() => Alert.alert('할 일 메뉴', undefined, [{ text: '취소', style: 'cancel' }, { text: '수정', onPress: () => openEdit(index) }, { text: '삭제', style: 'destructive', onPress: () => confirmDelete(index) }])} style={styles.moreButton}>
                  <Text style={[styles.more, { color: colors.muted }]}>⋮</Text>
                </Pressable>
              </View>
            ))
          )}

          <Text style={[styles.sectionTitle, { color: colors.text }]}>오늘 함께할 강아지</Text>
          <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.dogRow}>
            {DOGS.map((dog, index) => (
              <Pressable key={dog.name} accessibilityRole="radio" accessibilityState={{ selected: selectedDog === index }} accessibilityLabel={`${dog.name}${selectedDog === index ? ', 선택됨' : ', 선택하려면 누르기'}`} onPress={() => chooseDog(index)} style={({ pressed }) => [styles.dogCard, { backgroundColor: colors.surface, borderColor: selectedDog === index ? colors.primary : colors.border, borderWidth: selectedDog === index ? 2 : 1 }, pressed && styles.pressed]}>
                <DogAvatar type={dog.type} size={66} plainBackground />
                <Text style={[styles.dogName, { color: colors.text }]}>{dog.short}</Text>
              </Pressable>
            ))}
          </ScrollView>
        </ScrollView>
      </KeyboardAvoidingView>

      <Modal visible={editIndex !== null} transparent animationType="fade" onRequestClose={() => setEditIndex(null)}>
        <View style={styles.modalBackdrop}>
          <View style={[styles.modalCard, { backgroundColor: colors.surface }]}>
            <Text style={[styles.modalTitle, { color: colors.text }]}>할 일 수정</Text>
            <TextInput autoFocus value={editText} onChangeText={setEditText} style={[styles.editInput, { backgroundColor: colors.background, color: colors.text, borderColor: colors.border }]} />
            <View style={styles.modalActions}>
              <RoundedButton title="취소" onPress={() => setEditIndex(null)} colors={colors} secondary compact />
              <RoundedButton title="저장" onPress={saveEdit} colors={colors} compact />
            </View>
          </View>
        </View>
      </Modal>

      <Modal visible={Boolean(encouragement)} transparent animationType="fade" onRequestClose={() => setEncouragement(null)}>
        <View style={styles.modalBackdrop}>
          <View style={[styles.modalCard, styles.encouragementCard, { backgroundColor: colors.surface }]}>
              <View style={styles.wanderStage}>
                <DogAvatar
                  type={encouragement?.dogType ?? 0}
                  size={136}
                  motionKey={completionPulse}
                  animate
                  plainBackground
                  wander
                  wanderDistance={110}
                />
              </View>
            <Text style={[styles.modalTitle, { color: colors.text }]}>‘{encouragement?.title}’ 완료!</Text>
            <Text style={[styles.encouragement, { color: colors.primary }]}>{encouragement && messagesForKind(encouragement.messageKind ?? DOGS[encouragement.dogType]?.kind ?? 0)[messageIndex % messagesForKind(encouragement.messageKind ?? DOGS[encouragement.dogType]?.kind ?? 0).length]}</Text>
            <Text style={[styles.note, { color: colors.muted }]}>이 응원은 일상적인 동기 부여를 위한 메시지예요.</Text>
            <View style={styles.modalActions}>
              <RoundedButton title="다른 응원 보기" onPress={() => {
                const kind = encouragement?.messageKind ?? DOGS[encouragement?.dogType ?? 0]?.kind ?? 0;
                setMessageIndex((value) => (value + 1) % messagesForKind(kind).length);
              }} colors={colors} secondary compact />
              <RoundedButton title="확인" onPress={() => setEncouragement(null)} colors={colors} compact />
            </View>
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1 },
  flex: { flex: 1 },
  content: { paddingHorizontal: 16, paddingTop: 12, paddingBottom: 32 },
  rowBetween: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  headerCard: { flexDirection: 'row', alignItems: 'center', borderRadius: 18, borderWidth: 1, padding: 16 },
  headerDog: { width: 92, height: 92, borderRadius: 46, alignItems: 'center', justifyContent: 'center', overflow: 'hidden' },
  headerCopy: { flex: 1, paddingLeft: 16 },
  title: { fontSize: 24, lineHeight: 31, fontWeight: '700' },
  muted: { fontSize: 14, lineHeight: 21 },
  mutedBold: { fontSize: 14, fontWeight: '700', lineHeight: 20 },
  progressCard: { borderRadius: 16, paddingHorizontal: 16, paddingVertical: 14, marginTop: 16 },
  sectionLabel: { fontSize: 16, fontWeight: '700' },
  count: { fontSize: 16, fontWeight: '700' },
  progressTrack: { height: 8, borderRadius: 8, marginTop: 10, position: 'relative' },
  progressFill: { height: 8, borderRadius: 8 },
  progressDog: { position: 'absolute', width: 54, height: 54, top: -27, marginLeft: -27, alignItems: 'center', justifyContent: 'center' },
  card: { borderRadius: 16, borderWidth: 1, padding: 16, marginTop: 16 },
  inputRow: { flexDirection: 'row', alignItems: 'center', marginTop: 10 },
  input: { flex: 1, height: 52, borderRadius: 14, borderWidth: 1, paddingHorizontal: 14, fontSize: 16 },
  button: { height: 52, minWidth: 84, borderRadius: 14, borderWidth: 1, alignItems: 'center', justifyContent: 'center', paddingHorizontal: 16 },
  compactButton: { height: 44, minWidth: 78 },
  secondaryButton: { borderWidth: 1 },
  buttonText: { fontSize: 16, fontWeight: '700' },
  pressed: { opacity: 0.82, transform: [{ scale: 0.98 }] },
  sectionTitle: { fontSize: 18, fontWeight: '700', marginTop: 24, marginBottom: 10 },
  empty: { minHeight: 190, borderRadius: 16, borderWidth: 1, alignItems: 'center', justifyContent: 'center', padding: 24 },
  emptyTitle: { fontSize: 16, fontWeight: '700', marginTop: 10, marginBottom: 4 },
  taskCard: { minHeight: 72, borderRadius: 16, borderWidth: 1, padding: 8, flexDirection: 'row', alignItems: 'center', marginBottom: 10 },
  check: { width: 48, height: 48, borderRadius: 24, borderWidth: 2, alignItems: 'center', justifyContent: 'center' },
  checkMark: { color: '#FFFFFF', fontSize: 24, fontWeight: '700', marginTop: -2 },
  taskCopy: { flex: 1, paddingHorizontal: 8, minHeight: 52, justifyContent: 'center' },
  taskTitle: { fontSize: 16, lineHeight: 21, fontWeight: '700' },
  small: { fontSize: 12, lineHeight: 17, marginTop: 3 },
  moreButton: { width: 48, height: 52, alignItems: 'center', justifyContent: 'center' },
  more: { fontSize: 26, lineHeight: 28 },
  dogRow: { paddingBottom: 4 },
  dogCard: { width: 102, height: 124, borderRadius: 16, padding: 8, alignItems: 'center', marginRight: 8 },
  dogName: { fontSize: 12, fontWeight: '700', marginTop: 5 },
  avatar: { alignItems: 'center', justifyContent: 'center', overflow: 'hidden' },
  ear: { position: 'absolute', top: '25%', borderRadius: 8, transform: [{ rotate: '35deg' }] },
  leftEar: { left: '18%' },
  rightEar: { right: '18%', transform: [{ rotate: '-35deg' }] },
  body: { position: 'absolute', top: '56%' },
  head: { position: 'absolute', top: '27%', alignItems: 'center' },
  eye: { position: 'absolute', backgroundColor: '#48332C' },
  muzzle: { position: 'absolute', backgroundColor: '#F4C294' },
  nose: { position: 'absolute', backgroundColor: '#48332C' },
  glasses: { position: 'absolute', borderRadius: 7 },
  cross: { position: 'absolute', width: 15, height: 15, backgroundColor: '#FFFFFF', borderRadius: 4 },
  crossVertical: { position: 'absolute', left: 6, top: 2, width: 3, height: 11, backgroundColor: '#CF4D52' },
  crossHorizontal: { position: 'absolute', left: 2, top: 6, width: 11, height: 3, backgroundColor: '#CF4D52' },
  headband: { position: 'absolute', backgroundColor: '#F6DE5B', borderRadius: 5 },
  chefHat: { position: 'absolute', backgroundColor: '#FFFFFF', borderRadius: 7 },
  clipboard: { position: 'absolute', backgroundColor: '#FFFFFF', borderRadius: 2 },
  beret: { position: 'absolute', backgroundColor: '#425B91', borderRadius: 12, transform: [{ rotate: '-8deg' }] },
  loading: { flex: 1, alignItems: 'center', justifyContent: 'center', gap: 12 },
  loadingText: { fontSize: 14 },
  modalBackdrop: { flex: 1, backgroundColor: 'rgba(20, 12, 10, 0.48)', alignItems: 'center', justifyContent: 'center', padding: 20 },
  modalCard: { width: '100%', maxWidth: 420, borderRadius: 20, padding: 22 },
  encouragementCard: { alignItems: 'center' },
  wanderStage: { width: '100%', height: 170, overflow: 'hidden', alignItems: 'center', justifyContent: 'center' },
  modalTitle: { fontSize: 20, lineHeight: 27, fontWeight: '700', textAlign: 'center', marginTop: 10 },
  editInput: { height: 52, borderRadius: 14, borderWidth: 1, paddingHorizontal: 14, fontSize: 16, marginTop: 18 },
  modalActions: { flexDirection: 'row', gap: 8, width: '100%', justifyContent: 'flex-end', marginTop: 18 },
  encouragement: { fontSize: 20, lineHeight: 28, fontWeight: '700', textAlign: 'center', marginTop: 8 },
  note: { fontSize: 12, lineHeight: 18, textAlign: 'center', marginTop: 10 },
});
