import { CircularProgress } from '@mui/material';
import StyledAuthActionButton from './styles';

type Props = {
  text: string;
  loading: boolean;
};

const AuthActionButton = ({ text, loading }: Props) => {
  return (
    <StyledAuthActionButton
      disableElevation
      variant="contained"
      disabled={loading}
      type="submit"
    >
      {loading ? <CircularProgress size={24} /> : text}
    </StyledAuthActionButton>
  );
};

export default AuthActionButton;
