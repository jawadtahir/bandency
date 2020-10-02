from setuptools import setup, find_packages

setup(
    name="bandency",
    version='0.1',
    description="Testing Distributed Systems under Fault",
    packages=find_packages(exclude=["tests"]),
    author="Christoph Doblander",
    install_requires=[
        'libvirt-python',
        'numpy',
        'pandas',
        'seaborn',
        'jupyterlab',
        'html5lib',
        'beautifulsoup4',
        'tqdm',
        'executor',
        'quart',
        'quart-auth',
        'hypercorn',
        'gino',
        'sqlalchemy',
        'alembic'
    ],
    extras_require={
        'dev': [
            'python-language-server[all]'
        ],
        'test': [
            'pytest', 'pyflakes'
        ]
    }
)
