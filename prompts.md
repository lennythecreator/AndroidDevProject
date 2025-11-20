# Prompt for Stitch (or any AI UI Builder)

Design a mobile app called **AI Fitness App** for busy students who want structured accountability on their phones. The app acts as a lightweight personal coach that captures goals, tracks workouts and habits, and uses AI to deliver weekly insights and day-of nudges.

## Product Pillars
- **Audience:** College students and young professionals with limited time who need gentle reminders.
- **Value:** Help users set realistic weekly goals, celebrate consistency, and suggest the next best action.
- **Mobile Advantage:** Phone is always present, so use push reminders, glanceable widgets, camera for progress photos, and motion data (steps, heart rate) without extra hardware.

## Visual Direction
- Friendly, energetic palette (e.g., deep navy background, lime or coral accents).
- Rounded cards, pill buttons, subtle gradients.
- Typography: clean sans-serif (Inter / Google Sans). Use bold weights for numbers, light copy for guidance.
- Iconography: outline icons for navigation tabs, simple illustrations for AI insight cards.

## Required Screens
1. **Onboarding Flow**
   - Multi-step carousel collecting name, fitness focus (strength/cardio/mindfulness), preferred workout days, weekly hours, notification consent.
   - Show progress indicator (e.g., 1/4) and friendly microcopy.
2. **Dashboard (Home Tab)**
   - Hero section with greeting, current streak, circular progress ring for weekly goal.
   - “Today’s Focus” card with CTA (“Log run”, “Schedule recovery”).
   - Quick tiles: Log Workout, View Insights, Sync Wearables.
3. **Log Session Screen**
   - Form with exercise type chips, duration slider, intensity emojis, optional note field, photo upload button.
   - Save button + snackbar confirmation.
4. **AI Insights Tab**
   - Feed of AI-generated cards summarizing adherence (“3/4 cardio sessions hit”), highlight missed area, provide actionable button (“Plan makeup jog”).
   - Each card shows trend graphs (mini sparkline) and tags (Cardio, Strength).
5. **Profile / Settings Tab**
   - Sections for Goals (editable), Notifications schedule, Connected services (Google Fit toggle), Data export/download.
   - “Switch Challenge Mode” button for optional social leaderboard.

## Navigation Model
- Bottom tab bar with four tabs: Dashboard (home icon), Log (plus icon), Insights (spark icon), Profile (user icon).
- Global floating action button on dashboard to “Quick Log”.
- Flow examples:
  - Launch → Onboarding → Dashboard.
  - Dashboard card → Log Session → Success modal → Dashboard.
  - Insights card → CTA modal → Add to schedule.

## Key Components & States
- **Progress Ring Component:** shows percentage complete with weekly target, includes label “4 hrs of 5”.
- **AI Insight Card:** avatar of AI coach, summary text, stat row (sessions completed, consistency %), CTA button, overflow menu (Share, Mark done).
- **Reminder Modal:** time picker, day chips, toggle for “Send motivational quote”.
- **Leaderboard Widget (optional):** list of up to 5 friends with streak counts.

## Data & Content Notes
- Sample workout categories: Strength, Cardio, Mobility, Mindfulness.
- Habit metrics: weekly hours goal, streak count, consistency percentage.
- AI text tone: encouraging, concise (“You’re one session away from your weekly target. Schedule a 20-min jog?”).
- Empty states should encourage action (“No insights yet—log your first workout to unlock AI tips.”).

## Deliverables Expected
- High-fidelity mobile layouts (1080x1920) for each screen above.
- Component variants (default, filled, disabled) for inputs, chips, buttons, cards.
- Style tokens (colors, typography, spacing) exported for dev handoff.

Focus on clarity, motivational tone, and fast daily interactions. Optimize for one-handed use and ensure CTAs are reachable. Provide annotations for interactions (swipe, long press) when relevant.

