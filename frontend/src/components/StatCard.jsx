import React from 'react';

const StatCard = ({ label, value, subtext, icon: Icon, theme = 'blue' }) => {
  return (
    <div className="stat-card">
      <div>
        <div className="stat-label">{label}</div>
        <div className="stat-value">{value}</div>
        {subtext && <div className="stat-subtext">{subtext}</div>}
      </div>
      {Icon && (
        <div className={`stat-icon-wrapper stat-icon-${theme}`}>
          <Icon size={24} />
        </div>
      )}
    </div>
  );
};

export default StatCard;
