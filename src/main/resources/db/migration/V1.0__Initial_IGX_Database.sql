PRAGMA foreign_keys = ON;

CREATE TABLE Users (
    UserID INTEGER NOT NULL PRIMARY KEY,
    Username VARCHAR(40) NOT NULL,
    Password VARCHAR(200) NOT NULL
    
);

CREATE TABLE Rank (
    RankID INTEGER NOT NULL PRIMARY KEY,
    UserID INTEGER NOT NULL UNIQUE,
    GamesPlayed INTEGER NOT NULL DEFAULT(0),
    GamesWon INTEGER NOT NULL DEFAULT(0),
    GamesPlayedHumans INTEGER NOT NULL DEFAULT(0),
    GamesWonHumans INTEGER NOT NULL DEFAULT(0),
    Rank INTEGER NOT NULL DEFAULT(1500),
    FOREIGN KEY( UserID ) REFERENCES Users(UserID)
);

CREATE TABLE IGXConfig (
    key VARCHAR(200) NOT NULL PRIMARY KEY,
    value VARCHAR(200)
);