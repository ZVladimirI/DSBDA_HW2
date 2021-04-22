import argparse

"""
Main function
"""
if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Log geneartor')
    parser.add_argument('--output', type=str,
                        help='Output file name', default='data')

    options = parser.parse_args()

    mapping = {1: 'opened and viewed', 2: 'opened for preview', 3: 'did not interacted'}
    with open(options.output, 'w+') as f:
        for key in mapping:
            f.write(f'{key}, {mapping[key]}\n')
