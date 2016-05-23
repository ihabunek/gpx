CREATE EXTENSION citext;

--;;

CREATE TABLE "user" (
    "id" serial PRIMARY KEY,
    "name" text,
    "email" citext UNIQUE
);

--;;

CREATE TABLE "track" (
    "id" serial PRIMARY KEY,
    "slug" text UNIQUE,
    "user_id" integer REFERENCES "user",
    "name" text,
    "metadata" json,
    "stats" json
);

--;;

CREATE TABLE "segment" (
    "id" serial PRIMARY KEY,
    "track_id" integer REFERENCES "track",
    "lats" real[],
    "lons" real[],
    "elevations" real[],
    "times" timestamptz[]
);

--;;

CREATE TABLE "waypoint" (
    "id" serial PRIMARY KEY,
    "track_id" integer REFERENCES "track",
    "name" text,
    "lat" real,
    "lon" real,
    "elevation" real,
    "time" timestamptz
);