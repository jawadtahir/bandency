def menu(profile=False,
         deployment=False,
         recentchanges=False,
         documentation=False,
         faq=False,
         benchmarks=False,
         leaderboard=False,
         rawdata=False,
         feedback=False):
    return [
        ["Leaderboard", "layers", "/leaderboard", leaderboard],
        ["Profile", "home", "/profile", profile],
        ["Benchmarks", "layers", "/benchmarks", benchmarks],
        ["Deployment", "layers", "/deployment", deployment],
        ["Docs", "layers", "/docs", documentation],
        ["FAQ", "layers", "/faq", faq],
        ["Recent changes", "layers", "/recentchanges", recentchanges],
        ["Datasets", "rawdata", "/rawdata", rawdata],
        ["Feedback", "layers", "/feedback", feedback]]
