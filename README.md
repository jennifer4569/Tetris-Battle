# Tetris-Battle
## Jennifer Zhang and Michael Ruvinshteyn 

   We implemented Tetris Battle, a PvP version of the classic video game Tetris. The game involves trying to 
   achieve the highest possible score, while simultaneously trying to survive longer than your opponent. 
   Additionally, on top of attempting to outlast your opponent, you also have the ability to make the game 
   harder for your opponent by adding partially complete rows to their grid, which could lead to their 
   disqualification.

   There is be a central server (67.205.133.16), which accepts up to 32 clients at once. The central server 
   authenticates the users’ login, and stores various statistics about each user, as well as a leaderboard. 
   It also automatically performs matchmaking for each user who wishes to play.

   NOTE: To be able to run the program, you must have JDK installed.

## Work Distribution
- Jennifer: Networking and SQLite
- Michael: Frontend
