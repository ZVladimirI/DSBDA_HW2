from datetime import datetime, timedelta
import argparse
import random

"""
Generates random id from given range
"""
def generate_id(max_id):
    return random.randint(0, max_id)

"""
Static interaction type id generator implementation
"""
def get_map_ids():
    return [1, 2, 3]


"""
Main function
"""
if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Log geneartor')
    parser.add_argument('--start-date', type=str,
                        help='Start date and time for log file like dd.mm.YYYY/HH:MM:SS')
    parser.add_argument('--end-date', type=str,
                        help='End date for log file dd.mm.YYYY/HH:MM:SS')
    parser.add_argument('--period', type=int,
                        help='Seconds between records', default=600)
    parser.add_argument('--posts', type=int,
                        help='Number of posts in logs', default=1000)
    parser.add_argument('--users', type=int,
                        help='Number of users for logs', default=1000)
    parser.add_argument('--output', type=str,
                        help='Output file name', default='data')

    options = parser.parse_args()

    start = datetime.strptime(options.start_date.replace('/', ' '), '%d.%m.%Y %H:%M:%S')
    end = datetime.strptime(options.end_date.replace('/', ' '), '%d.%m.%Y %H:%M:%S')

    delta = timedelta(seconds=options.period)

    assert end > start

    with open(options.output, 'w+') as f:
        while start < end:
            start = start + delta
            num_users = random.randint(100, 1000)
            num_posts = random.randint(1, 100)
            for user in range(num_users):
                for post in range(num_posts):
                    log = f'{generate_id(options.posts)},{generate_id(options.users)},{start},' \
                          f'{random.choice(get_map_ids())}\n'
                    f.write(log)
