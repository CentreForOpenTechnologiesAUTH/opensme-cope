import os

BACKUP_DIR = 'home/shini/backup/'

index = 1
val = os.path.getmtime('/home/shini/backup/1')
for i in range(10):
	if (val > os.path.getmtime('/home/shini/backup/' + str(i + 1))):
		index = i + 1
		val = os.path.getmtime('/home/shini/backup/' + str(index))

os.system('cp -a src/* ' + BACKUP_DIR + str(index))
print "src backed-up in directory " + BACKUP_DIR + str(index)
