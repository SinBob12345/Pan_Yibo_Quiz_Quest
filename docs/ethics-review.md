# Ethics review log

Written by me (not by the AI agent). Before a feature is "done", I run the five-step
ACS check from my Assessment 2 presentation (slide 13) and record it here.
These entries are evidence for my Gibbs reflection.

## The five steps

1. **Stakeholders** — who is affected? (learners 13–17, parents/teachers, me as developer, the public) — Conduct 1.2.1(a)
2. **Test against values** — honesty (Ethics 2.1), privacy (2.2(d)), minimise harm (2.3.1), user control (Conduct 1.2.2(d))
3. **Resolve conflicts** — public interest comes first (Conduct 1.2.1)
4. **Least-harm alternative** — what is the least-data, most-transparent design that still works?
5. **Document & escalate** — what did I decide and why?

## Template

```
### <Feature> — <date>
1. Stakeholders:
2. Values at stake:
3. Conflict:
4. Option chosen (and options rejected):
5. Decision & reason:
What I would check again before release:
```

## Features to review

- [ ] Network calls to Open Trivia DB (what is sent, IP address)
- [ ] Choice of question categories (age-appropriate content)
- [ ] Stars and level unlocking (motivation vs manipulation)
- [ ] Optional timer
- [ ] Result screen buttons / no auto-advance
- [ ] Daily goal and "done for today" cue
- [ ] Statistics without streaks
- [ ] Review flashcards
- [ ] Data retention period
- [ ] Export and delete my data
- [ ] Cloud backup rules
- [ ] Flag a question
- [ ] Sound and animations (reduce motion)
- [ ] No notifications (why I decided not to send any)
