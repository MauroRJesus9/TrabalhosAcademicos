all: feed broker

feed: feed.c utils.c
	gcc -Wall -pthread -o feed feed.c utils.c

broker: manager.c utils.c
	gcc -Wall -pthread -o broker manager.c utils.c

clean:
	rm -f feed broker manager.o feed.o utils.o
