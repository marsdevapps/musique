drop table playlists if exists;

create table playlists (
    playlistID integer identity,
    name VARCHAR(30)
);

insert into playlists values (DEFAULT, 'Default');

drop table cue_sheets if exists;

create table cue_sheets (
    cueID INTEGER IDENTITY,
    embedded BOOLEAN,
    fileName VARCHAR(1000),
    cueSheet VARCHAR(20000)
);

drop table songs if exists;

create table songs (
    songID INTEGER IDENTITY,
    playlistID INTEGER,
    playlistPosition INTEGER,
    cueID INTEGER,

    filePath VARCHAR(1000) NOT NULL,

    artist VARCHAR(1000),
    album VARCHAR(1000),
    title VARCHAR(1000),
    trackNumber VARCHAR(1000),
    totalTracks VARCHAR(1000),
    discNumber VARCHAR(1000),
    totalDiscs VARCHAR(1000),
    year VARCHAR(1000),
    genre VARCHAR(1000),
    comment VARCHAR(1000),
    albumArtist VARCHAR(1000),
    extraTagFields VARCHAR(2000),

    bitrate INTEGER,
    samplerate INTEGER,
    codec VARCHAR(30),
    channels INTEGER,
    bps INTEGER,
    startPosition BIGINT,
    totalSamples BIGINT,
    subsongIndex INTEGER,
    extraHeaderFields VARCHAR(2000)
);

drop table playlist_columns if exists;

create table playlist_columns(
    id INTEGER IDENTITY,
    name VARCHAR(30) NOT NULL,
    expression VARCHAR(1000),
    size INTEGER,
    position INTEGER,
    orientation INTEGER,
    editable BOOLEAN
);

insert into playlist_columns values (DEFAULT, 'Playing', '$isPlaying()', 20, 0, 0, false);
insert into playlist_columns values (DEFAULT, 'Name', '[%artist% - ]$if3(%title%, %fileName%)', 20, 1, 0, false);
insert into playlist_columns values (DEFAULT, 'Length', '%length%', 20, 2, 0, false);
insert into playlist_columns values (DEFAULT, 'Album', '%album%', 20, 3, 0, false);
insert into playlist_columns values (DEFAULT, 'Date', '%year%', 20, 4, 0, false);