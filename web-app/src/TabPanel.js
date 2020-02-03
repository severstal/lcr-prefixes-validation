import React from 'react';
import Typography from '@material-ui/core/Typography/Typography';
import Box from '@material-ui/core/Box/Box';

export default ({ children, value, index }) => (
    <Typography hidden={value !== index}>
        <Box p={1}>{children}</Box>
    </Typography>
);
