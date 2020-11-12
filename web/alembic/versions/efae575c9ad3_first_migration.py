"""first migration

Revision ID: efae575c9ad3
Revises: 
Create Date: 2020-10-05 15:03:56.395665

"""
from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import postgresql

# revision identifiers, used by Alembic.
revision = 'efae575c9ad3'
down_revision = None
branch_labels = None
depends_on = None


def upgrade():
    # ### commands auto generated by Alembic - please adjust! ###
    op.create_table('groups',
    sa.Column('id', postgresql.UUID(), nullable=False),
    sa.Column('groupname', sa.Unicode(), nullable=True),
    sa.Column('password', sa.Unicode(), nullable=True),
    sa.Column('groupemail', sa.Unicode(), nullable=True),
    sa.Column('groupnick', sa.Unicode(), nullable=True),
    sa.PrimaryKeyConstraint('id')
    )
    # ### end Alembic commands ###


def downgrade():
    # ### commands auto generated by Alembic - please adjust! ###
    op.drop_table('groups')
    # ### end Alembic commands ###