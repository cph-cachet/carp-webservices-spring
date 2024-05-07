import { Typography } from '@mui/material';
import { styled } from '../../utils/theme';

export const LoginAdditionalActions = styled('div')(({ theme }) => ({
  display: 'flex',
  width: '100%',
  justifyContent: 'space-between',
  padding: '0px 0px 20px',
  alignItems: 'center',
  '& *': {
    margin: 0,
  },
  [theme.breakpoints.down('sm')]: {
    flexDirection: 'column',
    gap: 4,
    padding: '0px 0 18px',
  },
}));

export const LoginSeparator = styled('div')(({ theme }) => ({
  width: '100%',
  borderBottom: `1px solid ${theme.palette.grey[200]}`,
  textAlign: 'center',
  height: `calc(${theme.typography.h4_web.lineHeight} / 2)`,
  margin: '16px 0',
}));

export const LoginSeparatorText = styled(Typography)(({ theme }) => ({
  padding: '0 28px',
  backgroundColor: theme.palette.background.default,
  color: theme.palette.grey[500],
})) as typeof Typography;

export const LoginOauthOptions = styled('div')(({ theme }) => ({
  display: 'flex',
  width: '100%',
  justifyContent: 'center',
  gap: 48,
  paddingTop: 36,
  [theme.breakpoints.down('sm')]: {
    paddingTop: 24,
    gap: 20,
  },
}));
