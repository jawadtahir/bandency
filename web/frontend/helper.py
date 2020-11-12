def menu(profile=False, recentchanges=False, system_status=False, leaderboard=False, scheduled_benchmarks=False,
         feedback=False):
    return [
        ["Profile", "home", "/profile", profile],
        ["Recent changes", "layers", "/recentchanges", recentchanges],
        ["System status", "layers", "/systemstatus", system_status],
        ["Leaderboard", "layers", "/leaderboard", leaderboard],
        ["Scheduled Benchmarks", "layers", "/scheduledbenchmarks", scheduled_benchmarks],
        ["Feedback", "layers", "/feedback", feedback]]