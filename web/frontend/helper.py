def menu(profile=False,
         recentchanges=False,
         documentation=False,
         system_status=False,
         leaderboard=False,
         scheduled_benchmarks=False,
         rawdata=False,
         feedback=False):
    return [
        ["Profile", "home", "/profile", profile],
        ["Recent changes", "layers", "/recentchanges", recentchanges],
        ["Documentation", "layers", "/documentation", documentation],
        ["System status", "layers", "/systemstatus", system_status],
        ["Leaderboard", "layers", "/leaderboard", leaderboard],
        #["Scheduled Benchmarks", "layers", "/scheduledbenchmarks", scheduled_benchmarks],
        ["Datasets", "rawdata", "/rawdata", rawdata],
        ["Feedback", "layers", "/feedback", feedback]]
